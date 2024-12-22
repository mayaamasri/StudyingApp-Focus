package com.example.focus;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class HomePage extends Application {
    private static String studentName = "";
    private static int studentId;
    public static void setStudentName(String name) {
        studentName = name;
    }
    public static void setStudentId(int id) {
        studentId = id;
    }
    List<Button> menuButtons = new ArrayList<>();
    private VBox mainView;
    BorderPane root;
    private VBox menuBar;
    private Label welcomeLabel;

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        menuBar = new VBox(10);
        menuBar.setPadding(new Insets(10));
        menuBar.setStyle("-fx-background-color: black;");
        menuBar.setAlignment(Pos.TOP_LEFT);

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/img.png")));
        logo.setPreserveRatio(true);
        logo.setFitHeight(60);

        welcomeLabel = new Label("Hello " + studentName + "! \nClick 'Courses' to manage your classes or 'Tasks' to track your assignments!");
        welcomeLabel.setFont(Font.font("Alef", FontWeight.BOLD, 24));
        welcomeLabel.setAlignment(Pos.CENTER);
        welcomeLabel.setWrapText(true);

        // Create buttons for the top menu
        Button dashboardBtn = new Button("Home");
        Button courses = new Button("Courses");
        Button scheduleBtn = new Button("Schedule");
        Button tasksBtn = new Button("Tasks");
        Button notesBtn = new Button("Notes");
        Button pomodoro = new Button("Timer");

        // Set the width of the buttons to match the longest text, and add style
        Stream.of(dashboardBtn, courses, scheduleBtn, tasksBtn, notesBtn, pomodoro).forEach(button -> {
            button.setPrefWidth(150);
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
            button.setAlignment(Pos.BASELINE_LEFT);
            button.setMinHeight(50);
        });

        courses.setOnAction(event -> {
            resetButtonStyles(menuButtons); // Reset styles for all buttons
            courses.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
            showCoursesView();
        });
        scheduleBtn.setOnAction(event -> {
            resetButtonStyles(menuButtons); // Reset styles for all buttons
            scheduleBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
            showScheduleView();
        });
        tasksBtn.setOnAction(event -> {
            resetButtonStyles(menuButtons); // Reset styles for all buttons
            tasksBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: white;-fx-font-size: 16px;");
            showTasksView();
        });
        notesBtn.setOnAction(event -> {
            resetButtonStyles(menuButtons); // Reset styles for all buttons
            notesBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
            showNoteView();
        });
        pomodoro.setOnAction(event -> {
            resetButtonStyles(menuButtons); // Reset styles for all buttons
            pomodoro.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
            showPomodoroView();
        });

        menuButtons.add(dashboardBtn);
        menuButtons.add(courses);
        menuButtons.add(scheduleBtn);
        menuButtons.add(tasksBtn);
        menuButtons.add(notesBtn);
        menuButtons.add(pomodoro);

        menuBar.getChildren().addAll(logo, dashboardBtn, courses, scheduleBtn, tasksBtn, notesBtn, pomodoro);

        // Create a container for the main view
        mainView = new VBox(10);
        mainView.setPadding(new Insets(10));
        mainView.getChildren().add(welcomeLabel);
        root.setLeft(menuBar);
        root.setCenter(mainView);

        // Set up the scene
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Focus");
        primaryStage.show();
    }

    void resetButtonStyles(List<Button> buttons) {
        for (Button button : buttons) {
            button.setStyle("-fx-font-weight: normal; -fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        }
    }

    private void showScheduleView() {
        ScheduleView scheduleView = new ScheduleView(studentId);
        mainView.getChildren().clear();
        mainView.getChildren().add(scheduleView.getView());
    }
    private void showTasksView() {
        TaskManager taskView = new TaskManager(studentId);
        mainView.getChildren().clear();
        mainView.getChildren().add(taskView.getView());
    }
    private void showCoursesView() {
        Courses courses = new Courses(studentId);
        mainView.getChildren().clear();
        mainView.getChildren().add(courses.getView());
    }
    private void showPomodoroView() {
        PomodoroTimer pomodoro = new PomodoroTimer();
        mainView.getChildren().clear();
        mainView.getChildren().add(pomodoro.getView());
    }

    private void showNoteView() {
        NoteTaking note = new NoteTaking(studentId);
        mainView.getChildren().clear();
        mainView.getChildren().add(note.getView());
    }
    public static void main(String[] args) {
        launch(args);
    }
}
