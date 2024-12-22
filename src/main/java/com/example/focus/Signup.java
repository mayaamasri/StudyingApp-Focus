package com.example.focus;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signup extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/focus";
    private static final String USER = "root";
    private static final String PASS = "root";
    private Text messageText = new Text();

    @Override
    public void start(Stage primaryStage) {
        VBox root =new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F4F4F4;");

        // Sign up form
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: #F4F4F4; -fx-padding: 30; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: black ");
        form.setMaxWidth(300);
        form.setMaxHeight(400);

        // Logo placeholder
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logo.setPreserveRatio(true);
        logo.setFitHeight(60);

        Label signUpLabel = new Label("SIGN UP");
        signUpLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");


        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setId("form-text-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setId("form-text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setId("form-text-field");

        // Login Button
        Hyperlink loginBtn = new Hyperlink("Already have an account");
        loginBtn.setStyle("-fx-text-fill: black;");
        loginBtn.setOnAction(e ->
                new Login().start(primaryStage));

        //Sign Up Button
        Button signUpBtn = new Button("Create");
        signUpBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold");

        signUpBtn.setOnAction(e -> {
            if (isValidEmail(emailField.getText())) {
                handleSignUp(primaryStage,usernameField.getText(), emailField.getText(), passwordField.getText());
            } else {
                displayMessage("Invalid Email Format", javafx.scene.paint.Color.RED);
            }
        });

        form.getChildren().addAll(signUpLabel, usernameField, emailField, passwordField, signUpBtn, loginBtn, messageText);
        root.getChildren().addAll(logo, form);

        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("Sign Up");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (email == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private void handleSignUp(Stage stage, String username, String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // Check if email already exists
            String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkEmailQuery)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    displayMessage("Email already exists, login instead", javafx.scene.paint.Color.RED);
                    return;
                }
            }

            // Insert user into the database
            String query = "INSERT INTO users (username, email, pass) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, password);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        displayMessage("Sign Up Successful! User has been registered.", javafx.scene.paint.Color.GREEN);
                        HomePage.setStudentName(username);
                        HomePage.setStudentId(userId);
                        Platform.runLater(() -> {
                            try {
                                new HomePage().start(stage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException ex) {
            displayMessage("Database Error: " + ex.getMessage(), javafx.scene.paint.Color.RED);
        }
    }

    private void displayMessage(String message, Color color) {
        messageText.setText(message);
        messageText.setFill(color);
        StackPane.setAlignment(messageText, Pos.CENTER);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
