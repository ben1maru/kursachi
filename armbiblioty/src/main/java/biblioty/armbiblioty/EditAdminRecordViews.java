package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditAdminRecordViews {

    @FXML
    private TextField nameField;

    @FXML
    private Button saveBtn;

    private int recordId;
    private String tableName;
    private boolean dataLoaded = false;

    public void setRecordId(int recordId) {
        this.recordId = recordId;
        loadDataIfReady();
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        loadDataIfReady();
    }

    @FXML
    void initialize() {

        saveBtn.setOnAction(event -> saveChanges());
    }

    private void loadDataIfReady() {
        if (recordId > 0 && tableName != null && !dataLoaded) {
            loadRecordData();
            dataLoaded = true;
        }
    }

    private void loadRecordData() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name_" + (tableName.equals("author") ? "author" : "ganre") +
                             " AS name FROM " + tableName + " WHERE id = ?")) {

            stmt.setInt(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити дані: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (NullPointerException e) {
            showAlert("Помилка", "Не встановлено необхідні параметри для завантаження даних", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void saveChanges() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Попередження", "Введіть назву", Alert.AlertType.WARNING);
            return;
        }

        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE " + tableName + " SET name_" +
                             (tableName.equals("author") ? "author" : "ganre") +
                             " = ? WHERE id = ?")) {

            stmt.setString(1, nameField.getText().trim());
            stmt.setInt(2, recordId);
            stmt.executeUpdate();

            showAlert("Успіх", "Запис оновлено", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося оновити запис: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}