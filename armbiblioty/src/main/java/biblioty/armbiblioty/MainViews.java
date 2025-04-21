package biblioty.armbiblioty;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainViews {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button AdminBtn;

    @FXML
    private Button BookTheyTookBtn;

    @FXML
    private Button LibrariesBtn;

    @FXML
    void initialize() {
       if(Const.user.getIsAdmin()!=1){
           AdminBtn.setVisible(false);
       }
       AdminBtn.setOnAction(event -> {
           openWindow("AdminViews.fxml");
       });
       LibrariesBtn.setOnAction(event -> {
           openWindow("LibrariesViews.fxml");
       });
       BookTheyTookBtn.setOnAction(event -> {
           openWindow("BookTheyTookViews.fxml");
       });
    }
    /**
     * Метод для відкриття нового вікна.
     *
     * @param fxmlFile ім'я файлу FXML для завантаження
     */
    private void openWindow(String fxmlFile) {
        Stage stage = (Stage) AdminBtn.getScene().getWindow();
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

}
