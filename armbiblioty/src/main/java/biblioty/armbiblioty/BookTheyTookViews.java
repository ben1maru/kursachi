package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookTheyTookViews {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button backBtn;

    @FXML
    private VBox recordsVBox;

    @FXML
    private Pagination pagination;

    private static final int RECORDS_PER_PAGE = 8;
    private List<Record> allRecords = new ArrayList<>();

    public static class Record {
        private final int id;
        private final int bookId;
        private final String clientName;
        private final String bookName;
        private final String dateTook;

        public Record(int id, int bookId, String clientName, String bookName, String dateTook) {
            this.id = id;
            this.bookId = bookId;
            this.clientName = clientName;
            this.bookName = bookName;
            this.dateTook = dateTook;
        }

        public int getId() { return id; }
        public int getBookId() { return bookId; }
        public String getClientName() { return clientName; }
        public String getBookName() { return bookName; }
        public String getDateTook() { return dateTook; }
    }

    @FXML
    void initialize() {
        backBtn.setOnAction(event -> handleBack());
        loadRecords();
        setupPagination();
    }

    private void handleBack() {
        try {
            // Завантажуємо вікно LibrariesViews
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainViews.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

            // Закриваємо поточне вікно
            Stage currentStage = (Stage) backBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showAlert("Помилка", "Не вдалося повернутися: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadRecords() {
        allRecords.clear();
        DB db = new DB();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getDbConnection();
            String query = "SELECT bt.id, bt.id_book, bt.name_clients, bt.date_the_took, b.name_book " +
                    "FROM booktheytook bt " +
                    "JOIN books b ON bt.id_book = b.id_book";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                allRecords.add(new Record(
                        rs.getInt("id"),
                        rs.getInt("id_book"),
                        rs.getString("name_clients"),
                        rs.getString("name_book"),
                        rs.getString("date_the_took")
                ));
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити записи: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // З'єднання не закриваємо, як потрібно
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setupPagination() {
        if (allRecords.isEmpty()) {
            pagination.setPageCount(1);
            pagination.setDisable(true);
            recordsVBox.getChildren().clear();
            return;
        }

        int pageCount = (int) Math.ceil((double) allRecords.size() / RECORDS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setDisable(pageCount <= 1);
        pagination.setCurrentPageIndex(0);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayRecords(newIndex.intValue());
        });

        displayRecords(0);
    }

    private void displayRecords(int pageIndex) {
        recordsVBox.getChildren().clear();
        int startIndex = pageIndex * RECORDS_PER_PAGE;
        int endIndex = Math.min(startIndex + RECORDS_PER_PAGE, allRecords.size());

        for (int i = startIndex; i < endIndex; i++) {
            Record record = allRecords.get(i);
            HBox recordBox = createRecordBox(record);
            recordsVBox.getChildren().add(recordBox);
        }
    }

    private HBox createRecordBox(Record record) {
        HBox recordBox = new HBox(10);
        recordBox.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 5;");

        Label clientLabel = new Label("Клієнт: " + record.getClientName());
        Label bookLabel = new Label("Книга: " + record.getBookName());
        Label dateLabel = new Label("Дата видачі: " + record.getDateTook());

        Button editBtn = new Button("Редагувати");
        editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editBtn.setOnAction(event -> handleEdit(record));

        Button returnBtn = new Button("Повернув");
        returnBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        returnBtn.setOnAction(event -> handleReturn(record));

        recordBox.getChildren().addAll(clientLabel, bookLabel, dateLabel, editBtn, returnBtn);
        return recordBox;
    }

    private void handleEdit(Record record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditBookTheyTookViews.fxml"));
            Parent root = loader.load();
            EditBookTheyTookViews controller = loader.getController();
            controller.setRecordId(record.getId());
            controller.setBookId(record.getBookId());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadRecords();
            setupPagination();
        } catch (Exception e) {
            showAlert("Помилка", "Не вдалося відкрити форму редагування: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleReturn(Record record) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Підтвердження");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Ви впевнені, що книга повернута клієнтом '" + record.getClientName() + "'?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        DB db = new DB();
        Connection conn = null;

        try {
            conn = db.getDbConnection();
            String query = "DELETE FROM booktheytook WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, record.getId());
                stmt.executeUpdate();
            }

            showAlert("Успіх", "Запис успішно видалено", Alert.AlertType.INFORMATION);
            loadRecords();
            setupPagination();
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося видалити запис: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}