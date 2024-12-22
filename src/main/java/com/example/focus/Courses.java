package com.example.focus;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Courses {
    private GridPane coursesGrid;
    private final int studentId;
    private final Random random = new Random();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/focus";
    private static final String USER = "root";
    private static final String PASS = "root";
    private Text messageText = new Text();
    private Label label;
    private VBox root;
    int row, column;
    public Courses(int studentId) {
        root = new VBox(10);
        this.studentId = studentId;
        this.coursesGrid = new GridPane();
        coursesGrid.setHgap(10);
        coursesGrid.setVgap(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Button addCourseBtn = new Button("Add Course");
        addCourseBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold");
        addCourseBtn.setOnAction(event -> showAddCourseDialog());
        HBox addCourseContainer = new HBox(addCourseBtn);
        addCourseContainer.setAlignment(Pos.CENTER);
        addCourseContainer.setPadding(new Insets(10));

        Label title = new Label("My Courses:");
        title.setFont(Font.font("Alef", FontWeight.BOLD, 24));
        title.setAlignment(Pos.CENTER_LEFT);
        title.setPadding(new Insets(0, 0, 10, 0));

        VBox coursesContainer = new VBox(addCourseContainer, title, coursesGrid);
        root.getChildren().add(coursesContainer);

        // Create the welcome label.
        label = new Label("No courses added.");
        label.setFont(new Font("Arial", 20));
        label.setStyle("-fx-text-fill: #7C7C7C");
        label.setAlignment(Pos.CENTER_LEFT);

        if (!userHasCourses()){
            root.getChildren().add(label);
        }

        refreshCourseList();
    }

    private boolean userHasCourses() {
        String sql = "SELECT COUNT(*) FROM courses WHERE user_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void showAddCourseDialog() {
        Dialog<Pair<String, List<String>>> dialog = new Dialog<>();
        dialog.setTitle("Course");

        // Set the button types.
        ButtonType addScheduleButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addScheduleButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField courseName = new TextField();
        courseName.setPromptText("Course Name");

        // Create the checkboxes for day selection
        HBox daysBox = new HBox(10);
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        List<CheckBox> dayCheckBoxes = new ArrayList<>();
        for (String day : days) {
            CheckBox dayCheckBox = new CheckBox(day);
            dayCheckBoxes.add(dayCheckBox);
            daysBox.getChildren().add(dayCheckBox);
        }

        // Spinners for start time
        Spinner<Integer> startHourSpinner = createHourSpinner();
        Spinner<Integer> startMinuteSpinner = createMinuteSpinner();

        // Spinners for end time
        Spinner<Integer> endHourSpinner = createHourSpinner();
        Spinner<Integer> endMinuteSpinner = createMinuteSpinner();

        grid.add(new Label("Course Name:"), 0, 0);
        grid.add(courseName, 1, 0);
        grid.add(new Label("Days:"), 0, 1);
        grid.add(daysBox, 1, 1);
        grid.add(new Label("Start Time:"), 0, 2);
        grid.add(startHourSpinner, 1, 2);
        grid.add(new Label(":"), 2, 2);
        grid.add(startMinuteSpinner, 3, 2);
        grid.add(new Label("End Time:"), 0, 3);
        grid.add(endHourSpinner, 1, 3);
        grid.add(new Label(":"), 2, 3);
        grid.add(endMinuteSpinner, 3, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addScheduleButtonType) {
                String courseNameText = courseName.getText();
                List<String> selectedDays = dayCheckBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .toList();

                // Retrieve start and end times
                String startTime = String.format("%02d:%02d", startHourSpinner.getValue(), startMinuteSpinner.getValue());
                String endTime = String.format("%02d:%02d", endHourSpinner.getValue(), endMinuteSpinner.getValue());

                if (checkForTimeConflicts(courseNameText, selectedDays, startTime, endTime)) {
                    showAlert("Time Conflict", "There is a time conflict with an existing course.", Alert.AlertType.ERROR);
                    return null;
                } else if (isStartTimeBeforeEndTime(startTime, endTime)) {
                    // Combine the days and times into a List<String> with each entry as "day, start-end"
                    List<String> dayTimeCombinations = selectedDays.stream()
                            .map(day -> day + ", " + startTime + "-" + endTime)
                            .collect(Collectors.toList());

                    return new Pair<>(courseNameText, dayTimeCombinations);
                } else {
                    showEndTimeError();
                    return null;
                }
            }
            return null;
        });

        // Get the result from the dialog and process it
        Optional<Pair<String, List<String>>> result = dialog.showAndWait();
        result.ifPresent(courseDetails -> {
            String course = courseDetails.getKey();
            List<String> timesForDays = courseDetails.getValue();
            timesForDays.forEach(dayTime -> {
                String[] parts = dayTime.split(", ");
                String day = parts[0];
                String[] timeParts = parts[1].split("-");
                String startTime = timeParts[0];
                String endTime = timeParts[1];

                addCourseToDatabase(course, day, startTime, endTime);
            });
        });
    }
    private Spinner<Integer> createHourSpinner() {
        Spinner<Integer> hourSpinner = new Spinner<>();
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 19, 0));
        return hourSpinner;
    }
    private Spinner<Integer> createMinuteSpinner() {
        Spinner<Integer> minuteSpinner = new Spinner<>();
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 5));
        return minuteSpinner;
    }
    private boolean isStartTimeBeforeEndTime(String startTime, String endTime) {
        return Time.valueOf(startTime + ":00").before(Time.valueOf(endTime + ":00"));
    }
    private void showEndTimeError() {
        Alert alert = new Alert(Alert.AlertType.ERROR, "End time must be after start time.");
        alert.showAndWait();
    }
    private void addCourseToDatabase(String courseName, String day, String startTime, String endTime) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO courses (user_ID, courseName, courseDay, startTime, endTime) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, courseName);
                pstmt.setString(3, day);
                pstmt.setTime(4, Time.valueOf(startTime + ":00"));
                pstmt.setTime(5, Time.valueOf(endTime + ":00"));

                if (pstmt.executeUpdate() > 0) {
                    Platform.runLater(() -> {
                        refreshCourseList();
                        if (label.getParent() != null) {
                            root.getChildren().remove(label);
                        }
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private Node createCourseView(String courseName, String days, String startTime, String endTime) {
        VBox courseCard = new VBox(5); // Use a VBox for vertical layout within the card
        courseCard.setPadding(new Insets(10));
        courseCard.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: black; -fx-border-radius: 5;");

        Label nameLabel = new Label(courseName);
        nameLabel.setFont(Font.font("Alef", FontWeight.BOLD, 16));
        nameLabel.setAlignment(Pos.CENTER);

        Label timeLabel = new Label(startTime + " - " + endTime);
        timeLabel.setAlignment(Pos.CENTER);

        Label dayLabel = new Label(days);
        dayLabel.setAlignment(Pos.CENTER);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> deleteCourse(courseName));
        deleteButton.setAlignment(Pos.CENTER);

        Color pastelColor = Color.hsb(random.nextDouble() * 360, 0.5, 0.9, 0.5);
        String colorStyle = String.format("-fx-background-color: rgba(%d, %d, %d, 0.5);",
                (int) (pastelColor.getRed() * 255),
                (int) (pastelColor.getGreen() * 255),
                (int) (pastelColor.getBlue() * 255));

        courseCard.setMaxWidth(Double.MAX_VALUE); // Allow the box to expand
        GridPane.setFillWidth(courseCard, true); // Make the box fill its cell width
        GridPane.setHgrow(courseCard, Priority.ALWAYS);

        courseCard.setStyle(colorStyle + " -fx-border-color: black; -fx-border-width: 1;");

        courseCard.getChildren().addAll(nameLabel, timeLabel, dayLabel, deleteButton);
        return courseCard;
    }
    private boolean checkForTimeConflicts(String courseName, List<String> days, String startTime, String endTime) {
        String sql = "SELECT courseDay, startTime, endTime FROM courses WHERE user_ID = ? AND courseDay IN (";
        sql += days.stream().map(day -> "?").collect(Collectors.joining(", ")) + ")";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            int index = 2;
            for (String day : days) {
                pstmt.setString(index++, day);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String existingDay = rs.getString("courseDay");
                Time start = rs.getTime("startTime");
                Time end = rs.getTime("endTime");
                LocalTime newStartTime = LocalTime.parse(startTime);
                LocalTime newEndTime = LocalTime.parse(endTime);
                LocalTime existingStartTime = start.toLocalTime();
                LocalTime existingEndTime = end.toLocalTime();
                if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                    return true; // Conflict detected
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // No conflict
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void deleteCourse(String courseName) {
        String sql = "DELETE FROM courses WHERE courseName = ? AND user_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseName);
            pstmt.setInt(2, studentId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                displayMessage("Course successfully deleted.", javafx.scene.paint.Color.GREEN);
                refreshCourseList();
            } else {
                displayMessage("No course was deleted.", javafx.scene.paint.Color.RED);
            }
        } catch (SQLException e) {
            displayMessage("Error while deleting course: " + e.getMessage(), javafx.scene.paint.Color.RED);
        }
    }
    private void refreshCourseList() {
        coursesGrid.getChildren().clear(); // Clear the existing course views
        coursesGrid.getColumnConstraints().clear();
        row = 0;
        column = 0;

        for (int i = 0; i < 3; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / 3); // Each column gets one-third of the width
            colConst.setHgrow(Priority.ALWAYS); // Allow column to grow
            coursesGrid.getColumnConstraints().add(colConst); // Add layout constraints
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT courseName, GROUP_CONCAT(DISTINCT courseDay ORDER BY courseDay SEPARATOR ', ') as days, " +
                    "MIN(startTime) as startTime, MAX(endTime) as endTime " +
                    "FROM courses WHERE user_ID = ? GROUP BY courseName";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.isBeforeFirst()) { // No courses found, so show welcome label
                    coursesGrid.getChildren().add(label);
                } else {
                    root.getChildren().remove(label);
                    while (rs.next()) {
                        String courseName = rs.getString("courseName");
                        String days = rs.getString("days");
                        String startTime = rs.getString("startTime");
                        String endTime = rs.getString("endTime");
                        Node courseView = createCourseView(courseName, days, startTime, endTime);
                        coursesGrid.add(courseView, column, row);
                        column++;
                        if (column > 2) {  // Assuming 3 courses per row
                            column = 0;
                            row++;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void displayMessage(String message, Color color) {
        messageText.setText(message);
        messageText.setFill(color);
        StackPane.setAlignment(messageText, Pos.CENTER);
    }
//    private void editCourse(String courseName, String day, String startTime, String endTime) {
//        //
//    }
    public Node getView() {
        return root;
    }
}