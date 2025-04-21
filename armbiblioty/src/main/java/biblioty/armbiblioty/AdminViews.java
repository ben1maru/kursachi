package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminViews {

    @FXML private Button genresBtn;
    @FXML private Button authorsBtn;
    @FXML private Button backBtn;
    @FXML private VBox recordsVBox;
    @FXML private Pagination pagination;

    private static final int RECORDS_PER_PAGE = 8;
    private List<Record> allRecords = new ArrayList<>();
    private String currentTable = null;

    public static class Record {
        private final int id;
        private final String name;
        private final String tableName;

        public Record(int id, String name, String tableName) {
            this.id = id;
            this.name = name;
            this.tableName = tableName;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getTableName() { return tableName; }
    }

    @FXML
    void initialize() {
        genresBtn.setOnAction(event -> loadRecords("ganre"));
        authorsBtn.setOnAction(event -> loadRecords("author"));
        backBtn.setOnAction(event -> handleBack());

        setupPagination();
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainViews.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) backBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showAlert("Помилка", "Не вдалося повернутися: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadRecords(String tableName) {
        currentTable = tableName;
        allRecords.clear();
        DB db = new DB();

        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, name_" + (tableName.equals("author") ? "author" : "ganre") +
                             " AS name FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                allRecords.add(new Record(
                        rs.getInt("id"),
                        rs.getString("name"),
                        tableName
                ));
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити записи: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        setupPagination();
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
            recordsVBox.getChildren().add(createRecordBox(record));
        }
    }

    private HBox createRecordBox(Record record) {
        HBox recordBox = new HBox(10);
        recordBox.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 5;");
        recordBox.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(record.getName());
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Спейсер для вирівнювання кнопок праворуч
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Редагувати");
        editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editBtn.setOnAction(event -> handleEdit(record));

        Button deleteBtn = new Button("Видалити");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(event -> handleDelete(record));

        recordBox.getChildren().addAll(nameLabel, spacer, editBtn, deleteBtn);
        return recordBox;
    }

    private void handleEdit(Record record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditAdminRecordViews.fxml"));
            Parent root = loader.load();

            EditAdminRecordViews controller = loader.getController();
            controller.setRecordId(record.getId());
            controller.setTableName(record.getTableName());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadRecords(currentTable);
        } catch (IOException e) {
            showAlert("Помилка", "Не вдалося завантажити форму редагування: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(Record record) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Підтвердження");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Ви впевнені, що хочете видалити '" + record.getName() + "'?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        DB db = new DB();
        try (Connection conn = db.getDbConnection()) {
            conn.setAutoCommit(false);

            // Видаляємо зв'язані записи з таблиці books
            String deleteBooksQuery = "DELETE FROM books WHERE id_" +
                    (record.getTableName().equals("author") ? "author" : "ganre") +
                    " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteBooksQuery)) {
                stmt.setInt(1, record.getId());
                stmt.executeUpdate();
            }

            // Видаляємо сам запис
            String deleteRecordQuery = "DELETE FROM " + record.getTableName() + " WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteRecordQuery)) {
                stmt.setInt(1, record.getId());
                stmt.executeUpdate();
            }

            conn.commit();
            showAlert("Успіх", "Запис та всі пов'язані книги успішно видалено", Alert.AlertType.INFORMATION);
            loadRecords(currentTable);
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