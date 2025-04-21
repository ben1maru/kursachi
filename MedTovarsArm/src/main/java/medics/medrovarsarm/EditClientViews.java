package medics.medrovarsarm;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditClientViews {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private int clientId;

    public void setClientId(int clientId) {
        this.clientId = clientId;
        loadClientData();
    }

    @FXML
    void initialize() {
        saveBtn.setOnAction(event -> handleSave());
        cancelBtn.setOnAction(event -> handleCancel());
    }

    private void loadClientData() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT PIB, email, phone_number FROM Clients WHERE id = ?")) {

            stmt.setInt(1, clientId);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("PIB"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone_number"));
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити дані клієнта", Alert.AlertType.ERROR);
        }
    }

    private void handleSave() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            showAlert("Помилка", "Усі поля обов'язкові для заповнення", Alert.AlertType.ERROR);
            return;
        }

        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Clients SET PIB = ?, email = ?, phone_number = ? WHERE id = ?")) {

            stmt.setString(1, nameField.getText());
            stmt.setString(2, emailField.getText());
            stmt.setString(3, phoneField.getText());
            stmt.setInt(4, clientId);
            stmt.executeUpdate();

            showAlert("Успіх", "Дані клієнта успішно оновлено", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося оновити дані клієнта: " + e.getMessage(), Alert.AlertType.ERROR);
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