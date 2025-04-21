package medics.medrovarsarm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Клас Run є точкою входу в JavaFX додаток.
 * Він відповідає за ініціалізацію та запуск головного вікна програми.
 */
public class Run extends Application {

    /**
     * Метод start є початковою точкою запуску JavaFX додатка.
     * Він завантажує FXML файл для головного вікна авторизації і встановлює сцену на головний етап.
     *
     * @param stage головний етап, на якому буде встановлена сцена
     * @throws IOException якщо виникає помилка при завантаженні FXML файлу
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Run.class.getResource("Autentification.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Головний метод main запускає JavaFX додаток.
     * Викликає метод launch(), який ініціалізує JavaFX інфраструктуру.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        launch();
    }
}
