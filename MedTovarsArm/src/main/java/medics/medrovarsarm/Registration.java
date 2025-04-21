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
 * Клас-контролер для вікна реєстрації користувача.
 */
public class Registration {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnLogIn;

    @FXML
    private Button btnSignIn;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtRepitPassword;

    @FXML
    private TextField txtUserName;

    /**
     * Ініціалізує контролер після завантаження FXML.
     */
    @FXML
    void initialize() {
        // Обробник для кнопки реєстрації
        btnSignIn.setOnAction(event -> signUpNewUser());

        // Обработчик для кнопки повернення до автентифікації
        btnLogIn.setOnAction(event -> {
            try {
                switchScene("Autentification.fxml", "Не вдалося відкрити вікно автентифікації");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити вікно автентифікації",
                        "Виникла помилка: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Реєстрація нового користувача.
     */
    private void signUpNewUser() {
        String userName = txtUserName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String repeatPassword = txtRepitPassword.getText().trim();

        // Перевірка порожніх полів
        if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Помилка", "Незаповнені поля",
                    "Усі поля (ім'я, email, пароль, повтор пароля) повинні бути заповнені.");
            return;
        }

        // Перевірка довжини пароля
        if (password.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Короткий пароль",
                    "Пароль має містити щонайменше 8 символів.");
            return;
        }

        // Перевірка складності пароля
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Ненадійний пароль",
                    "Пароль має містити літери та цифри.");
            return;
        }

        // Перевірка збігу паролів
        if (!password.equals(repeatPassword)) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Невідповідність паролів",
                    "Паролі не збігаються. Перевірте введені дані.");
            return;
        }

        // Перевірка формату email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Некоректний email",
                    "Введіть дійсну адресу електронної пошти (наприклад, user@example.com).");
            return;
        }

        try {
            Autorize autorize = new Autorize();
            User user = new User(userName, email, password);
            autorize.signUpUser(user);

            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Реєстрація завершена",
                    "Ви успішно зареєструвалися! Увійдіть, використовуючи ваші дані.");
            clearFields();
            switchScene("Autentification.fxml", "Не вдалося відкрити вікно автентифікації");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити вікно автентифікації",
                    "Виникла помилка: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Помилка реєстрації",
                    "Не вдалося зареєструвати користувача. Можливо, email уже використовується.");
            e.printStackTrace();
        }
    }

    /**
     * Очищення полів форми.
     */
    private void clearFields() {
        txtUserName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtRepitPassword.setText("");
    }

    /**
     * Перехід до іншої сцени.
     * @param fxmlPath шлях до FXML-файлу
     * @param errorMessage повідомлення про помилку для алерту
     * @throws IOException якщо не вдалося завантажити FXML
     */
    private void switchScene(String fxmlPath, String errorMessage) throws IOException {
        Stage stage = (Stage) btnSignIn.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
    }

    /**
     * Відображення сповіщень користувачу.
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