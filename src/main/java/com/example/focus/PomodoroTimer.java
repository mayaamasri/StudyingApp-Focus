package com.example.focus;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class PomodoroTimer {
    private VBox view;
    private Label timerLabel;
    private Label titleLabel;
    private Label instructionLabel;
    private Timeline timeline;
    private Button startButton;
    private Button resetButton;
    private int workTime = 25 * 60;
    private int breakTime = 5 * 60;
    private int currentTime;
    private boolean isWorkTime = true;

    public PomodoroTimer() {
        view = new VBox(10);
        view.setStyle("-fx-padding: 20;");

        // Title label
        titleLabel = new Label("Pomodoro Timer:");
        titleLabel.setFont(Font.font("Alef", FontWeight.BOLD, 24));
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        titleLabel.setPadding(new Insets(0, 0, 10, 0));

        // Instruction label
        instructionLabel = new Label("Study for 25 minutes and take a 5 minutes break.");
        instructionLabel.setAlignment(Pos.CENTER_LEFT);
        instructionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

        // Timer label styling
        timerLabel = new Label("25:00");
        timerLabel.setAlignment(Pos.CENTER);
        timerLabel.setStyle("-fx-font-size: 150px; -fx-text-fill: black; -fx-font-weight: bold;");

        // Start and Reset buttons styling
        startButton = new Button("Start");
        resetButton = new Button("Reset");
        startButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold");
        resetButton.setStyle("-fx-background-color: #F4F4F4; -fx-border-color: black; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold");

        currentTime = workTime;

        startButton.setOnAction(e -> {
            if (timeline == null || timeline.getStatus() != Timeline.Status.RUNNING) {
                startTimer();
            } else {
                timeline.pause();
                startButton.setText("Resume");
            }
        });

        resetButton.setOnAction(e -> resetTimer());

        view.getChildren().addAll(titleLabel, instructionLabel, timerLabel, startButton, resetButton);
    }

    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        startButton.setText("Pause");
    }

    private void updateTimer() {
        currentTime--;
        if (currentTime < 0) {
            isWorkTime = !isWorkTime;
            currentTime = isWorkTime ? workTime : breakTime;
        }
        timerLabel.setText(formatTime(currentTime));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void resetTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        currentTime = workTime;
        timerLabel.setText(formatTime(workTime));
        startButton.setText("Start");
        isWorkTime = true;
    }
    public Node getView() {
        return view;
    }
}
