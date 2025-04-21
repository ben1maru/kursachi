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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
/**
 * Класс реєстрації
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
     * ініціалізація класу
     */
    @FXML
    void initialize() {

        btnSignIn.setOnAction(event -> {
            signUpNewUser();
        });
        btnLogIn.setOnAction(event-> {
            Stage stage = (Stage) btnLogIn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Autentification.fxml"));
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
     * Реэстрація користувача
     */
    private void signUpNewUser () {
        Autorize autorize= new Autorize();
        String userNameReg = txtUserName.getText();
        String passwordReg = txtPassword.getText();
        String emailReg = txtEmail.getText();
        String returnPass = txtRepitPassword.getText();
        if(txtPassword.getText().length()<8){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Помилка");
            alert.setContentText("Пароль надто короткий");
            alert.show();
            return;
        }
        if (!txtPassword.getText().equals(txtRepitPassword.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Помилка");
            alert.setContentText("Паролі не співпадають");
            alert.show();

        } else {
            ArrayList<String> arrList = new ArrayList<String>();
            arrList.addAll(Arrays.asList(userNameReg, passwordReg, emailReg,returnPass));

            if (arrList.stream().anyMatch(text -> text.equals(""))) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Помилка");
                alert.setContentText("Заповніть всі поля");
                alert.show();
                return;
            }
            User user = new User(userNameReg, emailReg, passwordReg);
            autorize.signUpUser(user);
            txtUserName.setText("");
            txtRepitPassword.setText("");
            txtPassword.setText("");
            txtEmail.setText("");
            Stage stage = (Stage) btnSignIn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Autentification.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            stage.setScene(new Scene(root));
        }

    }

}
