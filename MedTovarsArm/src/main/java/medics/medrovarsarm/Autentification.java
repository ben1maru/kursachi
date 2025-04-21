package medics.medrovarsarm;

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
        // Обробник події для кнопки входу
        btnLogIn.setOnAction(event -> {
            String loginText = txtEmail.getText().trim();
            String loginPassword = txtPassword.getText().trim();

            if (loginText.isEmpty() || loginPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Помилка", "Порожні поля", "Будь ласка, заповніть поля логіну та пароля.");
            } else {
                loginUser(loginText, loginPassword);
            }
        });

        // Обробник події для кнопки реєстрації
        btnSgnIn.setOnAction(event -> {
            try {
                Stage stage = (Stage) btnSgnIn.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Registration.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити вікно реєстрації", "Виникла помилка: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Метод для автентифікації користувача.
     * @param loginText логін користувача
     * @param passwordTxt пароль користувача
     */
    private void loginUser(String loginText, String passwordTxt) {
        try {
            Autorize autorize = new Autorize();
            User userFromDatabase = autorize.getUser(loginText, passwordTxt);
            if (userFromDatabase != null) {
                Const.user = userFromDatabase;
                Stage stage = (Stage) btnLogIn.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainViews.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                System.out.println("Успішний вхід: " + userFromDatabase.getIsAdmin());
            } else {
                showAlert(Alert.AlertType.ERROR, "Помилка автентифікації", "Невірний логін або пароль",
                        "Перевірте правильність введених даних і спробуйте знову.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити головне вікно", "Виникла помилка: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Помилка автентифікації",
                    "Виникла неочікувана помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Метод для відображення сповіщень користувачу.
     * @param alertType тип алерту (інформація, помилка, попередження тощо)
     * @param title заголовок сповіщення
     * @param header заголовок повідомлення
     * @param content текст повідомлення
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}