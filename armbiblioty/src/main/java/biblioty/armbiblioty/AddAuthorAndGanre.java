package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddAuthorAndGanre {
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnRequest;

    @FXML
    private TextField NameField;

    private String mode; // "author" або "ganre"

    public void setMode(String mode) {
        this.mode = mode;
        // Налаштування тексту кнопки та підказки залежно від режиму
        if ("author".equals(mode)) {
            btnRequest.setText("Додати автора");
            NameField.setPromptText("Ім'я автора");
        } else if ("ganre".equals(mode)) {
            btnRequest.setText("Додати жанр");
            NameField.setPromptText("Назва жанру");
        }
    }

    @FXML
    void initialize() {
        btnRequest.setOnAction(event -> {
            try {
                addEntry();
            } catch (SQLException e) {
                showAlert("Помилка", "Не вдалося додати запис: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void addEntry() throws SQLException {
        if (NameField.getText() == null || NameField.getText().trim().isEmpty()) {
            showAlert("Попередження", "Будь ласка, введіть " + ("author".equals(mode) ? "ім'я автора" : "назву жанру"), Alert.AlertType.WARNING);
            return;
        }

        DB db = new DB();
        Connection conn = db.getDbConnection();
        String query;
        String tableName;

        if ("author".equals(mode)) {
            tableName = "author";
            query = "INSERT INTO author (name_author) VALUES (?)";
        } else if ("ganre".equals(mode)) {
            tableName = "ganre";
            query = "INSERT INTO ganre (name_ganre) VALUES (?)";
        } else {
            showAlert("Помилка", "Невідомий режим", Alert.AlertType.ERROR);
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, NameField.getText().trim());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert("Успіх", "Запис успішно додано до " + ("author".equals(mode) ? "авторів" : "жанрів"), Alert.AlertType.INFORMATION);
                closeWindow();
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnRequest.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}