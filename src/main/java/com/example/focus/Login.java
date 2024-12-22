package com.example.focus;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;

public class Login extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/focus";
    private static final String USER = "root";
    private static final String PASS = "root";
    private Text messageText = new Text();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F4F4F4;");

        //Login form
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: F4F4F4; -fx-padding: 30; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: black");
        form.setMaxWidth(300);
        form.setMaxHeight(400);

        // Logo placeholder
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logo.setPreserveRatio(true);
        logo.setFitHeight(60);

        Label loginLabel = new Label("LOG IN");
        loginLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setId("form-text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setId("form-text-field");

        //Login Button
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin(primaryStage, emailField.getText(), passwordField.getText()));
        loginButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold");

        //Create Account Button
        Hyperlink createAccountButton = new Hyperlink("Create New Account");
        createAccountButton.setStyle("-fx-text-fill: black;");
        createAccountButton.setOnAction(e -> {
            new Signup().start(primaryStage);
        });

        form.getChildren().addAll(loginLabel, emailField, passwordField, loginButton, createAccountButton, messageText);
        root.getChildren().addAll(logo, form);

        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    private void handleLogin(Stage stage, String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT user_ID, username, pass FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("pass");
                    if (storedPassword.equals(password)) {
                        String username = rs.getString("username");
                        int userId = rs.getInt("user_ID");
                        HomePage.setStudentName(username);
                        HomePage.setStudentId(userId);
                        new HomePage().start(stage);
                    } else {
                        displayMessage("Wrong password", Pos.CENTER, javafx.scene.paint.Color.RED);
                    }
                } else {
                    displayMessage("Email does not exist, sign up instead", Pos.CENTER, javafx.scene.paint.Color.RED);
                }
            } catch (SQLException ex) {
                displayMessage("Database Error: " + ex.getMessage(), Pos.CENTER, javafx.scene.paint.Color.RED);
            }
        } catch (SQLException ex) {
            displayMessage("Database Connection Failed: " + ex.getMessage(), Pos.CENTER, javafx.scene.paint.Color.RED);
        }
    }
    private void displayMessage(String message, Pos position, javafx.scene.paint.Color color) {
        messageText.setText(message);
        messageText.setFill(color);
        StackPane.setAlignment(messageText, position);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
