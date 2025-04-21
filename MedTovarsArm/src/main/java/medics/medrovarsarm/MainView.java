package medics.medrovarsarm;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Клас MainView відповідає за головний інтерфейс програми, що дозволяє користувачам переходити між різними розділами.
 */
public class MainView {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button ProductsBtn;

    @FXML
    private Button OrdersBtn;

    @FXML
    private Button adminPanel;
    /**
     * Ініціалізує компонент, перевіряє права адміністратора та налаштовує обробники подій для кнопок.
     */
    @FXML
    void initialize() {
        if (Const.user.getIsAdmin() != 1) {
            adminPanel.setVisible(false);  // Приховуємо панель адміністратора, якщо користувач не є адміністратором
        }
        ProductsBtn.setOnAction(event -> {
                openWindow("ProductsViews.fxml");

        });
        OrdersBtn.setOnAction(event -> {

                openWindow("OrderViews.fxml");
        });


        adminPanel.setOnAction(event -> openWindow("AdminViews.fxml"));

    }

    /**
     * Метод для відкриття нового вікна.
     *
     * @param fxmlFile ім'я файлу FXML для завантаження
     */
    private void openWindow(String fxmlFile) {
        Stage stage = (Stage) adminPanel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(fxmlFile));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root));
    }

    /**
     * Метод для відображення інформаційного повідомлення.
     *
     * @param message текст повідомлення
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
