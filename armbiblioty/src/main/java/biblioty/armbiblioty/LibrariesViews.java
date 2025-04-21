package biblioty.armbiblioty;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class LibrariesViews {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableColumn<Book, Integer> ISBNColumn;
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> AutorBook;
    @FXML private TableColumn<Book, String> GanreColumn;
    @FXML private TableColumn<Book, String> NameBook;
    @FXML private TableColumn<Book, Integer> QuantityColumn;
    @FXML private TextField SearchField;
    @FXML private Button AddBooks;
    @FXML private Button BackBtn;
    @FXML private Button AddAuthor;
    @FXML private Button AddGanre;
    @FXML private TableColumn<Book, String> StatusColumn;
    @FXML private TableColumn<Book, Integer> yearWrite;
    @FXML private TableColumn<Book, Void> ActionColumns;

    private ObservableList<Book> booksData = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        AddBooks.setOnAction(event -> handleAddBook());
        BackBtn.setOnAction(event -> handleBack());
        AddAuthor.setOnAction(event -> handleAddAuthor());
        AddGanre.setOnAction(event -> handleAddGanre());

        try {
            // Налаштування колонок
            configureTableColumns();

            // Завантаження даних
            loadBooksData();

            // Налаштування пошуку
            setupSearch();

            // Підсвічування рядків
            setupRowHighlighting();

        } catch (Exception e) {
            showErrorAlert("Помилка ініціалізації", e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureTableColumns() {
        NameBook.setCellValueFactory(new PropertyValueFactory<>("name"));
        AutorBook.setCellValueFactory(new PropertyValueFactory<>("author"));
        GanreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearWrite.setCellValueFactory(new PropertyValueFactory<>("year"));
        QuantityColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        ISBNColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        ActionColumns.setCellFactory(createActionCellFactory());
    }

    private void setupRowHighlighting() {
        booksTable.setRowFactory(tv -> new TableRow<Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (book == null || empty) {
                    setStyle("");
                } else {
                    setStyle(book.getAvailableQuantity() <= 0 ? "-fx-background-color: #ffcccc;" : "");
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Book> filteredData = new FilteredList<>(booksData, p -> true);

        SearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return book.getName().toLowerCase().contains(lowerCaseFilter) ||
                        book.getAuthor().toLowerCase().contains(lowerCaseFilter) ||
                        book.getGenre().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(book.getYear()).contains(lowerCaseFilter) ||
                        String.valueOf(book.getIsbn()).contains(lowerCaseFilter);
            });
        });

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(booksTable.comparatorProperty());
        booksTable.setItems(sortedData);
    }

    private synchronized void loadBooksData() {
        booksData.clear();
        DB db = new DB();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getDbConnection();
            conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), 5000);

            String query = "SELECT b.id_book, b.name_book, a.name_author, g.name_ganre, " +
                    "b.write_year, b.quantity, s.status, b.isbn, " +
                    "(SELECT COUNT(*) FROM booktheytook bt WHERE bt.id_book = b.id_book) AS issued_count " +
                    "FROM books b " +
                    "JOIN author a ON b.id_author = a.id " +
                    "JOIN ganre g ON b.id_ganre = g.id " +
                    "JOIN status s ON b.id_status = s.id";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int totalQuantity = rs.getInt("quantity");
                int issuedCount = rs.getInt("issued_count");
                int availableQuantity = totalQuantity - issuedCount;

                booksData.add(new Book(
                        rs.getInt("id_book"),
                        rs.getString("name_book"),
                        rs.getString("name_author"),
                        rs.getString("name_ganre"),
                        rs.getInt("write_year"),
                        totalQuantity,
                        availableQuantity,
                        rs.getString("status"),
                        rs.getInt("isbn")
                ));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка завантаження даних", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // З'єднання не закриваємо, оскільки воно завжди відкрите
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Callback<TableColumn<Book, Void>, TableCell<Book, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button editBtn = new Button("Редагувати");
            private final Button deleteBtn = new Button("Видалити");
            private final Button issueBtn = new Button("Видати");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn, issueBtn);

            {
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                issueBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

                editBtn.setOnAction(event -> editBook(getCurrentBook()));
                deleteBtn.setOnAction(event -> deleteBook(getCurrentBook()));
                issueBtn.setOnAction(event -> handleIssueBook(getCurrentBook()));
            }

            private Book getCurrentBook() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getCurrentBook();
                    issueBtn.setDisable(book.getAvailableQuantity() <= 0);
                    setGraphic(buttons);
                }
            }
        };
    }

    private void handleIssueBook(Book book) {
        if (book.getAvailableQuantity() <= 0) {
            showWarningAlert("Попередження", "Книга відсутня в бібліотеці!");
            return;
        }

        try {
            issueBookWithRetry(book, 3);
            loadBooksData();
        } catch (Exception e) {
            showErrorAlert("Помилка видачі книги", e.getMessage());
        }
    }

    private void issueBookWithRetry(Book book, int maxRetries) throws Exception {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                issueBook(book);
                return;
            } catch (SQLException e) {
                lastException = e;
                attempts++;
                if (attempts >= maxRetries) break;

                try {
                    Thread.sleep(100 * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }

        throw lastException != null ? lastException : new Exception("Невідома помилка");
    }

    private void issueBook(Book book) throws SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookTookViews.fxml"));
        try {
            Parent root = loader.load();
            BookTookViews controller = loader.getController();
            controller.setBookId(book.getId());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            throw new SQLException("Помилка при відкритті форми видачі: " + e.getMessage());
        }
    }

    private void editBook(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditBooksViews.fxml"));
            Parent root = loader.load();

            EditBooksViews controller = loader.getController();
            controller.setBookId(book.getId());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBooksData();
        } catch (Exception e) {
            showErrorAlert("Помилка редагування", e.getMessage());
        }
    }

    private void deleteBook(Book book) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Підтвердження");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Ви впевнені, що хочете видалити книгу '" + book.getName() + "'?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        DB db = new DB();
        Connection conn = null;

        try {
            conn = db.getDbConnection();
            conn.setAutoCommit(false);
            conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), 5000);

            String deleteIssuedQuery = "DELETE FROM booktheytook WHERE id_book = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteIssuedQuery)) {
                stmt.setInt(1, book.getId());
                stmt.executeUpdate();
            }

            String deleteBookQuery = "DELETE FROM books WHERE id_book = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBookQuery)) {
                stmt.setInt(1, book.getId());
                stmt.executeUpdate();
            }

            conn.commit();
            loadBooksData();
            showInfoAlert("Успіх", "Книгу успішно видалено");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showErrorAlert("Помилка видалення", "Не вдалося видалити книгу: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    // З'єднання не закриваємо
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddBooksViews.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBooksData();
        } catch (Exception e) {
            showErrorAlert("Помилка додавання книги", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            // Завантажуємо головне вікно (MainViews)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainViews.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

            // Закриваємо поточне вікно
            Stage currentStage = (Stage) BackBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showErrorAlert("Помилка повернення", e.getMessage());
        }
    }

    @FXML
    private void handleAddAuthor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddAuthorAndGanre.fxml"));
            Parent root = loader.load();
            AddAuthorAndGanre controller = loader.getController();
            controller.setMode("author"); // Встановлюємо режим для автора
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBooksData(); // Оновлюємо дані після додавання
        } catch (Exception e) {
            showErrorAlert("Помилка додавання автора", e.getMessage());
        }
    }

    @FXML
    private void handleAddGanre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddAuthorAndGanre.fxml"));
            Parent root = loader.load();
            AddAuthorAndGanre controller = loader.getController();
            controller.setMode("ganre"); // Встановлюємо режим для жанру
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBooksData(); // Оновлюємо дані після додавання
        } catch (Exception e) {
            showErrorAlert("Помилка додавання жанру", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Book {
        private final int id;
        private final String name;
        private final String author;
        private final String genre;
        private final int year;
        private final int totalQuantity;
        private final int availableQuantity;
        private final String status;
        private final int isbn;

        public Book(int id, String name, String author, String genre, int year,
                    int totalQuantity, int availableQuantity, String status, int isbn) {
            this.id = id;
            this.name = name;
            this.author = author;
            this.genre = genre;
            this.year = year;
            this.totalQuantity = totalQuantity;
            this.availableQuantity = availableQuantity;
            this.status = status;
            this.isbn = isbn;
        }

        // Гетери
        public int getId() { return id; }
        public String getName() { return name; }
        public String getAuthor() { return author; }
        public String getGenre() { return genre; }
        public int getYear() { return year; }
        public int getTotalQuantity() { return totalQuantity; }
        public int getAvailableQuantity() { return availableQuantity; }
        public String getStatus() { return status; }
        public int getIsbn() { return isbn; }
    }
}