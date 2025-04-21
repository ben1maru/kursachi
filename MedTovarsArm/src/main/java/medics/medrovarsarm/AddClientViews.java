package medics.medrovarsarm;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Контролер для вікна додавання нового клієнта.
 */
public class AddClientViews {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField pibField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    /**
     * Ініціалізує контролер, налаштовує обробники подій для кнопок.
     */
    @FXML
    void initialize() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
    }

    /**
     * Зберігає нового клієнта в базі даних.
     */
    private void handleSave() {
        String pib = pibField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (pib.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showErrorAlert("Помилка", "Усі поля обов'язкові для заповнення");
            return;
        }

        DB db = new DB();
        Connection conn = null;

        try {
            conn = db.getDbConnection();
            String query = "INSERT INTO Clients (PIB, email, phone_number) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pib);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.executeUpdate();

            showInfoAlert("Успіх", "Клієнта успішно додано");
            handleCancel();

        } catch (SQLException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося додати клієнта: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    // Connection is not closed as per DB class assumption
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Закриває вікно без збереження.
     */
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Показує повідомлення про помилку.
     *
     * @param title   Заголовок повідомлення
     * @param message Текст повідомлення
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показує інформаційне повідомлення.
     *
     * @param title   Заголовок повідомлення
     * @param message Текст повідомлення
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}