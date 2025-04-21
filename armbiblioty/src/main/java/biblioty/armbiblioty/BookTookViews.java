package biblioty.armbiblioty;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BookTookViews {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnRequest;

    @FXML
    private TextField txtPIB;  // Змінено з PasswordField на TextField для зручності введення

    private int bookId;

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    @FXML
    void initialize() {
        btnRequest.setOnAction(event -> {
            try {
                issueBook();
            } catch (SQLException e) {
                showAlert("Помилка", "Не вдалося видати книгу: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void issueBook() throws SQLException {
        // Перевірка заповненості поля ПІБ
        if (txtPIB.getText() == null || txtPIB.getText().trim().isEmpty()) {
            showAlert("Попередження", "Будь ласка, введіть ПІБ клієнта", Alert.AlertType.WARNING);
            return;
        }

        DB db = new DB();
        Connection conn = db.getDbConnection(); // Використовуємо існуюче з'єднання

        // Отримуємо поточну дату у форматі "день-місяць-рік"
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Додаємо запис про видачу
        String query = "INSERT INTO booktheytook (id_book, name_clients, date_the_took) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setString(2, txtPIB.getText().trim());
            stmt.setString(3, currentDate);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert("Успіх", "Книгу успішно видано клієнту: " + txtPIB.getText(), Alert.AlertType.INFORMATION);
                closeWindow();
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося видати книгу: " + e.getMessage(), Alert.AlertType.ERROR);
            throw e; // Перезкидання виключення для обробки вище
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnRequest.getScene().getWindow();
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