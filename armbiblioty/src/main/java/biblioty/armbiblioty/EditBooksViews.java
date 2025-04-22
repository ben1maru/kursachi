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
import javafx.stage.Stage;

public class EditBooksViews {

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

    private int bookId;

    public void setBookId(int bookId) {
        this.bookId = bookId;
        loadBookData();
    }

    @FXML
    void initialize() {
        loadAuthors();
        loadGenres();

        AddBooks.setOnAction(event -> {
            try {
                updateBook();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Помилка", "Не вдалося оновити книгу", Alert.AlertType.ERROR);
            }
        });
    }

    private void loadBookData() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection()) {
            String query = "SELECT b.name_book, a.name_author, g.name_ganre, " +
                    "b.write_year, b.isbn, b.quantity " +
                    "FROM books b " +
                    "JOIN author a ON b.id_author = a.id " +
                    "JOIN ganre g ON b.id_ganre = g.id " +
                    "WHERE b.id_book = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, bookId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    NameBookField.setText(rs.getString("name_book"));
                    authorChoicBox.setValue(rs.getString("name_author"));
                    GanreChoicBox.setValue(rs.getString("name_ganre"));
                    writeYearField.setText(String.valueOf(rs.getInt("write_year")));
                    ISBNField.setText(String.valueOf(rs.getInt("isbn")));
                    QuantityField.setText(String.valueOf(rs.getInt("quantity")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAuthors() {
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
                    authorMap.put(name, Integer.valueOf(id));
                }
                authorChoicBox.setItems(authors);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGenres() {
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
                    ganreMap.put(name, Integer.valueOf(id));
                }
                GanreChoicBox.setItems(genres);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBook() throws SQLException {
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

            if (quantity < 0) {
                showAlert("Попередження", "Кількість не може бути від'ємною", Alert.AlertType.WARNING);
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
        try (Connection conn = db.getDbConnection()) {
            String query = "UPDATE books SET " +
                    "name_book = ?, " +
                    "id_author = ?, " +
                    "id_ganre = ?, " +
                    "write_year = ?, " +
                    "isbn = ?, " +
                    "quantity = ? " +
                    "WHERE id_book = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, NameBookField.getText());
                stmt.setInt(2, authorId);
                stmt.setInt(3, ganreId);
                stmt.setInt(4, Integer.parseInt(writeYearField.getText()));
                stmt.setInt(5, Integer.parseInt(ISBNField.getText()));
                stmt.setInt(6, Integer.parseInt(QuantityField.getText()));
                stmt.setInt(7, bookId);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert("Успіх", "Дані книги успішно оновлено", Alert.AlertType.INFORMATION);
                    closeWindow();
                } else {
                    showAlert("Помилка", "Не вдалося оновити дані книги", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) AddBooks.getScene().getWindow();
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