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

import java.sql.*;

public class NoteTaking {
    private HBox view;
    private TextField titleField;
    private TextArea contentArea;
    private ObservableList<String> notesTitles;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/focus";
    private static final String USER = "root";
    private static final String PASS = "root";
    private int studentId;
    ListView<String> notesList;
    private Button deleteButton;

    public NoteTaking(int studentId) {
        this.studentId = studentId;

        VBox leftColumn = new VBox(10);
        leftColumn.setStyle(" -fx-spacing: 8;");

        VBox rightColumn = new VBox(10);
        rightColumn.setStyle(" -fx-padding: 20; -fx-spacing: 8;");

        Label notesLabel = new Label("Notes");
        notesLabel.setFont(Font.font("Alef", FontWeight.BOLD, 24));
        notesLabel.setAlignment(Pos.CENTER_LEFT);
        notesLabel.setPadding(new Insets(0, 0, 10, 0));

        titleField = new TextField();
        titleField.setPromptText("Note Title");
        titleField.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        contentArea = new TextArea();
        contentArea.setPromptText("Note Content");
        contentArea.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        notesTitles = FXCollections.observableArrayList();
        notesList = new ListView<>(notesTitles);
        notesList.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        this.deleteButton = new Button("Delete Note");
        deleteButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold");
        setupDeleteButton();

        Button saveButton = new Button("Save Note");
        saveButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold");
        saveButton.setOnAction(e -> saveNote());

        Button newNoteButton = new Button("New Note");
        newNoteButton.setStyle("-fx-background-color: black; -fx-text-fill: white;-fx-font-weight: bold");
        newNoteButton.setOnAction(e -> clearFields());

        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadNoteContent(newVal));

        loadNotesTitles();
        leftColumn.getChildren().addAll(notesLabel, notesList, newNoteButton);
        rightColumn.getChildren().addAll(titleField, contentArea, saveButton, deleteButton);

        view = new HBox(leftColumn, rightColumn);
    }
    private void loadNotesTitles() {
        // Load note titles from the database and add them to the notesTitles list
        String sql = "SELECT title FROM notes WHERE user_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notesTitles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void setupDeleteButton() {
        deleteButton.setOnAction(event -> {
            String selectedNoteTitle = notesList.getSelectionModel().getSelectedItem();
            if (selectedNoteTitle != null) {
                deleteSelectedNote(selectedNoteTitle);
            }
        });
    }
    private void loadNoteContent(String title) {
        // Load the content of the note with the given title
        String sql = "SELECT content FROM notes WHERE user_ID = ? AND title = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                contentArea.setText(rs.getString("content"));
                titleField.setText(title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void saveNote() {
        String title = titleField.getText();
        String content = contentArea.getText();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title and content cannot be empty.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO notes (user_ID, title, content) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, title);
                pstmt.setString(3, content);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    updateNotesList();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the note.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save the note: " + e.getMessage());
        }
    }
    private void deleteSelectedNote(String title) {
        String sql = "DELETE FROM notes WHERE title = ? AND user_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setInt(2, studentId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                notesTitles.remove(title); // Remove from the ListView
                showAlert(Alert.AlertType.INFORMATION, "Success", "Note deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the note.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error while deleting the note: " + e.getMessage());
        }
    }
    private void clearFields() {
        titleField.clear();
        contentArea.clear();
    }
    private void updateNotesList() {
        notesTitles.clear();
        loadNotesTitles();
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Node getView() {
        return view;
    }
}
