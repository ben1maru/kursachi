package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Клас-контролер для вікна автентифікації користувача.
 */
public class Autentification {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnLogIn;

    @FXML
    private Button btnSgnIn;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    /**
     * Ініціалізує контролер після завантаження користувальницького інтерфейсу (FXML).
     */
    @FXML
    void initialize() {
        // Перевірка підключення до бази даних
        checkDatabaseConnection();

        // Обробник події для кнопки входу
        btnLogIn.setOnAction(event -> {
            String loginText = txtEmail.getText().trim();
            String loginPassword = txtPassword.getText().trim();

            if (loginText.isEmpty() || loginPassword.isEmpty()) {
                showAlert("Попередження", "Будь ласка, введіть логін і пароль", Alert.AlertType.WARNING);
            } else {
                loginUser(loginText, loginPassword);
            }
        });

        // Обробник події для кнопки реєстрації
        btnSgnIn.setOnAction(event -> {
            Stage stage = (Stage) btnSgnIn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Registration.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                showAlert("Помилка", "Не вдалося відкрити вікно реєстрації: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
                return;
            }
            Parent root = loader.getRoot();
            stage.setScene(new Scene(root));
        });
    }

    /**
     * Перевіряє підключення до бази даних і виводить повідомлення.
     */
    private void checkDatabaseConnection() {
        try {
            DB db = new DB();
            Connection conn = db.getDbConnection();
            if (conn != null && !conn.isClosed()) {
                showAlert("Інформація", "Підключення до бази даних встановлено", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Помилка", "Не вдалося підключитися до бази даних", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Помилка підключення до бази даних: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Метод для автентифікації користувача.
     * @param loginText логін користувача
     * @param passwordTxt пароль користувача
     */
    public void loginUser(String loginText, String passwordTxt) {
        Autorize autorize = new Autorize();
        User userFromDatabase = autorize.getUser(loginText, passwordTxt);
        if (userFromDatabase != null) {
            Const.user = userFromDatabase;
            Stage stage = (Stage) btnLogIn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("MainViews.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                showAlert("Помилка", "Не вдалося відкрити головне вікно: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
                return;
            }
            Parent root = loader.getRoot();
            stage.setScene(new Scene(root));
        } else {
            showAlert("Помилка", "Невірний логін або пароль", Alert.AlertType.ERROR);
        }
    }

    /**
     * Показує сповіщення користувачу.
     * @param title заголовок сповіщення
     * @param message текст сповіщення
     * @param type тип сповіщення
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}