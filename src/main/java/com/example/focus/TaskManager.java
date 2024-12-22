package com.example.focus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/focus";
    private static final String USER = "root";
    private static final String PASS = "root";
    private VBox taskView;
    private ComboBox<String> courseComboBox;
    private ObservableList<String> coursesList;
    private int studentId;
    private FileChooser fileChooser;
    private File chosenFile;
    private ObservableList<Node> tasksList;
    public TaskManager(int studentId) {
        this.studentId = studentId;
        this.tasksList = FXCollections.observableArrayList();
        ListView<Node> tasksListView = new ListView<>(tasksList);

        Label title = new Label("My Tasks:");
        title.setFont(Font.font("Alef", FontWeight.BOLD, 24));
        title.setAlignment(Pos.CENTER_LEFT);
        title.setPadding(new Insets(0, 0, 10, 0));

        this.taskView = new VBox(10);
        this.taskView.setStyle("-fx-padding: 20;");

        this.coursesList = FXCollections.observableArrayList();

        populateCoursesList();
        populateTasksList();
        createAddTaskButton();
        taskView.getChildren().addAll(title, tasksListView);
    }
    private Node createTaskItem(String courseName, String taskName, String materialType, Date deadline) {
        HBox taskItem = new HBox(10);
        CheckBox checkBox = new CheckBox();

        // Format the time left label
        Label timeLeftLabel = new Label(formatTimeLeft(deadline.toLocalDate()));

        // Set color depending on whether the deadline has passed or not
        if (deadline.toLocalDate().isBefore(LocalDate.now())) {
            timeLeftLabel.setStyle("-fx-text-fill: red;"); // Overdue
        } else {
            timeLeftLabel.setStyle("-fx-text-fill: green;"); // Time left
        }

        Label label = new Label(String.format("%s - %s - %s - %s", courseName, taskName, materialType, deadline));

        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                label.setText(label.getText() + " - Completed!");
                label.setStyle("-fx-text-fill: grey;");
                timeLeftLabel.setVisible(false);
            } else {
                // Update to the original text if unchecked
                label.setText(String.format("%s - %s - %s - %s", courseName, taskName, materialType, deadline));
                timeLeftLabel.setText(formatTimeLeft(deadline.toLocalDate()));
                timeLeftLabel.setVisible(true);
                if (deadline.toLocalDate().isBefore(LocalDate.now())) {
                    label.setStyle("-fx-text-fill: black;");
                    timeLeftLabel.setStyle("-fx-text-fill: red;");
                } else {
                    label.setStyle("-fx-text-fill: black;");
                    timeLeftLabel.setStyle("-fx-text-fill: green;");
                }
            }
        });

        // Create context menu for deleting task
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> deleteTask(taskName));
        contextMenu.getItems().add(deleteItem);

        // Set context menu on the label
        label.setContextMenu(contextMenu);

        // Optional: set on mouse click if you want to show context menu with left-click
        label.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // or set your own condition
                contextMenu.show(label, event.getScreenX(), event.getScreenY());
            }
        });

        taskItem.getChildren().addAll(checkBox, label, timeLeftLabel);
        return taskItem;
    }
    private String formatTimeLeft(LocalDate deadline) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (daysLeft < 0) {
            return "Overdue";
        }
        else if (daysLeft ==0)
            return "Due today";
         else {
            if(daysLeft==1)
                return daysLeft + " day left";
            else return daysLeft + " days left";
        }
    }
    private void deleteTask(String taskName) {
        // SQL to delete the task
        String sql = "DELETE FROM materials WHERE title = ? AND user_ID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, taskName);
            pstmt.setInt(2, studentId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Task deleted successfully.");
                refreshTasksList();
            } else {
                System.out.println("No task was deleted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void populateTasksList() {
        String sql = "SELECT c.courseName, m.title, m.materialType, m.deadline " +
                "FROM materials m " +
                "JOIN courses c ON m.course_ID = c.course_ID " +
                "WHERE m.user_ID = ? " +
                "ORDER BY m.deadline ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create a task item node for each task and add it to the tasksList
                    Node taskItem = createTaskItem(
                            rs.getString("courseName"),
                            rs.getString("title"),
                            rs.getString("materialType"),
                            rs.getDate("deadline")
                    );
                    tasksList.add(taskItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void refreshTasksList() {
        tasksList.clear();
        populateTasksList();
    }
    private void populateCoursesList() {
        List<String> studentCourses = getStudentCourses(studentId);
        coursesList.addAll(studentCourses);
    }

    private List<String> getStudentCourses(int studentId) {
        List<String> courses = new ArrayList<>();

        // SQL query to select courses for a specific student
        String sql = "SELECT DISTINCT courseName FROM courses WHERE user_ID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(rs.getString("courseName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
    private void createAddTaskButton() {
        Button addTaskButton = new Button("Add Task");
        addTaskButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold");
        addTaskButton.setOnAction(event -> showAddTaskDialog());
        taskView.getChildren().add(addTaskButton);

    }
    private void showAddTaskDialog() {
        // Create and initialize the dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Task");

        // Course drop-down
        courseComboBox = new ComboBox<>(coursesList);
        courseComboBox.setPromptText("Choose course");

        // Task name input
        TextField taskNameField = new TextField();
        taskNameField.setPromptText("Task name");

        // Material type selection
        ToggleGroup materialToggleGroup = new ToggleGroup();
        RadioButton projectRadio = new RadioButton("Project");
        projectRadio.setToggleGroup(materialToggleGroup);
        RadioButton assignmentRadio = new RadioButton("Assignment");
        assignmentRadio.setToggleGroup(materialToggleGroup);
        RadioButton examRadio = new RadioButton("Exam");
        examRadio.setToggleGroup(materialToggleGroup);
        RadioButton revisionRadio = new RadioButton("Revision");
        revisionRadio.setToggleGroup(materialToggleGroup);

        fileChooser = new FileChooser();
        Button uploadButton = new Button("Upload Document");
        uploadButton.setOnAction(e -> {
            chosenFile = fileChooser.showOpenDialog(null);
        });

        // Deadline picker
        DatePicker deadlinePicker = new DatePicker();
        // Layout
        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(courseComboBox, taskNameField,
                projectRadio, assignmentRadio, examRadio, revisionRadio, deadlinePicker, uploadButton);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set content
        dialog.getDialogPane().setContent(dialogLayout);

        // Process results
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                addTaskToDatabase(
                        courseComboBox.getValue(),
                        taskNameField.getText(),
                        ((RadioButton) materialToggleGroup.getSelectedToggle()).getText(),
                        deadlinePicker.getValue()
                );
            }
            return null;
        });
        dialog.showAndWait();
    }
    private void addTaskToDatabase(String courseName, String taskName, String materialType, LocalDate deadline) {
        String findCourseIdSql = "SELECT course_ID FROM courses WHERE courseName = ? AND user_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement findCourseIdStmt = conn.prepareStatement(findCourseIdSql)) {

            findCourseIdStmt.setString(1, courseName);
            findCourseIdStmt.setInt(2, studentId);
            ResultSet resultSet = findCourseIdStmt.executeQuery();

            if (resultSet.next()) {
                int courseId = resultSet.getInt("course_ID");

                String insertSql = "INSERT INTO materials (course_ID, title, materialType, deadline, user_ID, document_path) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, courseId);
                    insertStmt.setString(2, taskName);
                    insertStmt.setString(3, materialType);
                    insertStmt.setDate(4, Date.valueOf(deadline));
                    insertStmt.setInt(5, studentId);

                    if (chosenFile != null) {
                        insertStmt.setString(6, chosenFile.getAbsolutePath());
                    } else {
                        insertStmt.setString(6, null);
                    }

                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Task added to database successfully.");
                        refreshTasksList();
                    } else {
                        System.out.println("No task was added to the database.");
                    }
                }
            } else {
                System.out.println("Course not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Node getView() {
        return taskView;
    }
}