package biblioty.armbiblioty;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class AddBooksViews {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button AddBooks;

    @FXML
    private ChoiceBox<String> GanreChoicBox;
    private Map<String, Integer> ganreMap = new HashMap<>();

    @FXML
    private TextField ISBNField;

    @FXML
    private TextField NameBookField;

    @FXML
    private TextField QuantityField;

    @FXML
    private ChoiceBox<String> authorChoicBox;
    private Map<String, Integer> authorMap = new HashMap<>();

    @FXML
    private TextField writeYearField;

    @FXML
    void initialize() {
        loadAuthors();
        loadGenres();

        AddBooks.setOnAction(event -> {
            try {
                addNewBook();
            } catch (SQLException e) {
                handleDatabaseError(e);
            }
        });
    }

    private void loadAuthors() {
        try {
            DB db = new DB();
            try (Connection conn = db.getDbConnection()) {
                String query = "SELECT id, name_author FROM author";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    ObservableList<String> authors = FXCollections.observableArrayList();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name_author");
                        authors.add(name);
                        authorMap.put(name, id);
                    }
                    authorChoicBox.setItems(authors);
                }
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити авторів", Alert.AlertType.ERROR);
        }
    }

    private void loadGenres() {
        try {
            DB db = new DB();
            try (Connection conn = db.getDbConnection()) {
                String query = "SELECT id, name_ganre FROM ganre";
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {

                    ObservableList<String> genres = FXCollections.observableArrayList();
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name_ganre");
                        genres.add(name);
                        ganreMap.put(name, id);
                    }
                    GanreChoicBox.setItems(genres);
                }
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити жанри", Alert.AlertType.ERROR);
        }
    }

    private void addNewBook() throws SQLException {
        // Перевірка заповненості полів
        if (NameBookField.getText().isEmpty() ||
                authorChoicBox.getValue() == null ||
                GanreChoicBox.getValue() == null ||
                writeYearField.getText().isEmpty() ||
                ISBNField.getText().isEmpty() ||
                QuantityField.getText().isEmpty()) {

            showAlert("Попередження", "Будь ласка, заповніть всі поля", Alert.AlertType.WARNING);
            return;
        }

        // Перевірка числових полів
        try {
            Integer.parseInt(writeYearField.getText());
            Integer.parseInt(ISBNField.getText());
            int quantity = Integer.parseInt(QuantityField.getText());

            if (quantity <= 0) {
                showAlert("Попередження", "Кількість має бути більше 0", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Помилка", "Рік, ISBN та кількість мають бути числами", Alert.AlertType.ERROR);
            return;
        }

        // Отримання ID автора та жанру
        int authorId = authorMap.get(authorChoicBox.getValue());
        int ganreId = ganreMap.get(GanreChoicBox.getValue());

        DB db = new DB();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            conn.setAutoCommit(false); // Починаємо транзакцію

            String query = "INSERT INTO books (name_book, id_author, id_ganre, write_year, isbn, quantity, id_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 1)"; // id_status = 1 (Доступна)

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, NameBookField.getText());
                stmt.setInt(2, authorId);
                stmt.setInt(3, ganreId);
                stmt.setInt(4, Integer.parseInt(writeYearField.getText()));
                stmt.setInt(5, Integer.parseInt(ISBNField.getText()));
                stmt.setInt(6, Integer.parseInt(QuantityField.getText()));

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Підтверджуємо транзакцію
                    showAlert("Успіх", "Книгу успішно додано", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    conn.rollback(); // Відкатуємо транзакцію
                    showAlert("Помилка", "Не вдалося додати книгу", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Відкатуємо транзакцію при помилці
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Повертаємо автоматичний режим
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleDatabaseError(SQLException e) {
        if (e.getMessage().contains("database is locked")) {
            showAlert("Помилка", "База даних зайнята. Спробуйте ще раз через декілька секунд.", Alert.AlertType.ERROR);
        } else {
            showAlert("Помилка", "Помилка бази даних: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        NameBookField.clear();
        authorChoicBox.setValue(null);
        GanreChoicBox.setValue(null);
        writeYearField.clear();
        ISBNField.clear();
        QuantityField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}