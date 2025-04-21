package medics.medrovarsarm;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditCategoryViews {

    @FXML private TextField nameField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private int categoryId;

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
        loadCategoryData();
    }

    @FXML
    void initialize() {
        saveBtn.setOnAction(event -> handleSave());
        cancelBtn.setOnAction(event -> handleCancel());
    }

    private void loadCategoryData() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name FROM category WHERE id = ?")) {

            stmt.setInt(1, categoryId);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити дані категорії", Alert.AlertType.ERROR);
        }
    }

    private void handleSave() {
        if (nameField.getText().isEmpty()) {
            showAlert("Помилка", "Назва категорії не може бути порожньою", Alert.AlertType.ERROR);
            return;
        }

        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE category SET name = ? WHERE id = ?")) {

            stmt.setString(1, nameField.getText());
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();

            showAlert("Успіх", "Категорію успішно оновлено", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося оновити категорію: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
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