package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

            if (!loginText.equals("") && !loginPassword.equals(""))
                loginUser(loginText, loginPassword);
            else
                System.out.println("login is empty ");
        });

        // Обробник події для кнопки реєстрації
        btnSgnIn.setOnAction(event -> {
            Stage stage = (Stage) btnSgnIn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Registration.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            stage.setScene(new Scene(root));
        });
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
            System.out.println(Const.user.getIsAdmin());
            Stage stage = (Stage) btnLogIn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("MainViews.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            stage.setScene(new Scene(root));
            System.out.println("login");
        }
    }
}
