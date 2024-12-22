package com.example.focus;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

import java.sql.*;
import java.util.Random;

public class ScheduleView {
    private GridPane scheduleGrid;
    private final Random random = new Random();
    private int studentId;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/focus";
    private static final String USER = "root";
    private static final String PASS = "root";
    private static final int startHour = 8;
    private static final int endHour = 19;
    public ScheduleView(int studentId) {
        this.studentId = studentId;
        scheduleGrid = new GridPane();
        initializeScheduleGrid();
        populateSchedule();
    }

    private void initializeScheduleGrid() {
        scheduleGrid.setGridLinesVisible(true);
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / daysOfWeek.length);
            scheduleGrid.getColumnConstraints().add(colConst);
            scheduleGrid.getColumnConstraints().get(i).setHgrow(Priority.ALWAYS);
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-padding: 5px; -fx-font-weight: bold; -fx-alignment: center");
            scheduleGrid.add(dayLabel, i+1, 0);
        }

        for (int i = startHour; i < endHour; i++) {
            for (int j = 0; j < 2; j++) {
                RowConstraints rowConst = new RowConstraints();
                rowConst.setMinHeight(30);
                scheduleGrid.getRowConstraints().add(rowConst);

                rowConst = new RowConstraints();
                rowConst.setMinHeight(30);
                scheduleGrid.getRowConstraints().add(rowConst);

                Label hourLabel = new Label(String.format("%02d:00", i));
                hourLabel.setStyle("-fx-padding: 5px; -fx-font-weight: bold; -fx-alignment: center");
                scheduleGrid.add(hourLabel, 0, (i - startHour) * 2 + 1);

                Label halfHourLabel = new Label(String.format("%02d:30", i));
                halfHourLabel.setStyle("-fx-padding: 5px; -fx-font-weight: bold; -fx-alignment: center");
                scheduleGrid.add(halfHourLabel, 0, (i - startHour) * 2 + 2);
            }
        }
    }

    private void populateSchedule() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT courseName, courseDay, startTime, endTime FROM courses WHERE user_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String courseName = rs.getString("courseName");
                    String day = rs.getString("courseDay");
                    String startTime = rs.getString("startTime");
                    String endTime = rs.getString("endTime");
                    addToScheduleGrid(courseName, day, startTime, endTime);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addToScheduleGrid(String courseName, String day, String startTime, String endTime) {
        int dayColumn = dayToColumnIndex(day);
        int startRow = timeToRowIndex(startTime);
        int endRow = timeToRowIndex(endTime);

        int rowSpan = endRow - startRow;
        ColumnConstraints colConst = scheduleGrid.getColumnConstraints().get(dayColumn);

        Color pastelColor = Color.hsb(random.nextDouble() * 360, 0.5, 0.9, 0.5);
        String colorStyle = String.format("-fx-background-color: rgba(%d, %d, %d, 0.8);",
                (int) (pastelColor.getRed() * 255),
                (int) (pastelColor.getGreen() * 255),
                (int) (pastelColor.getBlue() * 255));

        Label courseLabel = new Label(courseName + "\n" + startTime + "-" + endTime);
        courseLabel.setStyle(colorStyle + "-fx-border-color: black; -fx-padding: 5; -fx-alignment: center;");
        courseLabel.prefWidthProperty().bind(scheduleGrid.widthProperty().multiply(colConst.getPercentWidth()).divide(100));
        double minHeight = rowSpan * 30.0;
        courseLabel.setMinHeight(minHeight);
        scheduleGrid.add(courseLabel, dayColumn + 1, startRow , 1, rowSpan);
    }

    private int dayToColumnIndex(String day) {
        switch (day) {
            case "Mon": return 0;
            case "Tue": return 1;
            case "Wed": return 2;
            case "Thu": return 3;
            case "Fri": return 4;
            default: return -1; // Invalid day
        }
    }

    private int timeToRowIndex(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        int rowIndex = (hour - startHour) * 2;
        rowIndex += (minutes >= 30) ? 1 : 0;

        return rowIndex + 1;
    }

    public Node getView() {
        ScrollPane scrollPane = new ScrollPane(scheduleGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        return scrollPane;
    }
}