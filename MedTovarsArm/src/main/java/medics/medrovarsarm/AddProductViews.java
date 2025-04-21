package medics.medrovarsarm;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Контролер для вікна додавання нового продукту.
 */
public class AddProductViews {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField nameField;
    @FXML private TextField countryField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ChoiceBox<String> categoryChoiceBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ObservableList<String> categories = FXCollections.observableArrayList();

    /**
     * Ініціалізує контролер, завантажує категорії та налаштовує обробники подій.
     */
    @FXML
    void initialize() {
        loadCategories();
        categoryChoiceBox.setItems(categories);

        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
    }

    /**
     * Завантажує список категорій з бази даних.
     */
    private void loadCategories() {
        DB db = new DB();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getDbConnection();
            String query = "SELECT name FROM category";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося завантажити категорії: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Зберігає новий продукт у базі даних з id_status = 1.
     */
    private void handleSave() {
        String name = nameField.getText().trim();
        String country = countryField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String category = categoryChoiceBox.getValue();

        if (name.isEmpty() || country.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() ||
                category == null) {
            showErrorAlert("Помилка", "Усі поля обов'язкові для заповнення");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка", "Ціна та кількість повинні бути коректними числами");
            return;
        }

        DB db = new DB();
        Connection conn = null;

        try {
            conn = db.getDbConnection();
            conn.setAutoCommit(false);

            String categoryQuery = "SELECT id FROM category WHERE name = ?";
            PreparedStatement categoryStmt = conn.prepareStatement(categoryQuery);
            categoryStmt.setString(1, category);
            ResultSet categoryRs = categoryStmt.executeQuery();
            int categoryId = categoryRs.next() ? categoryRs.getInt("id") : -1;

            if (categoryId == -1) {
                showErrorAlert("Помилка", "Некоректна категорія");
                return;
            }

            String query = "INSERT INTO Product (name_product, country, price, quantity, id_status, id_category) " +
                    "VALUES (?, ?, ?, ?, 1, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, country);
            stmt.setDouble(3, price);
            stmt.setString(4, String.valueOf(quantity));
            stmt.setInt(5, categoryId);
            stmt.executeUpdate();

            conn.commit();
            showInfoAlert("Успіх", "Продукт успішно додано");
            handleCancel();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showErrorAlert("Помилка бази даних", "Не вдалося додати продукт: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Закриває вікно без збереження.
     */
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Показує повідомлення про помилку.
     *
     * @param title   Заголовок повідомлення
     * @param message Текст повідомлення
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Показує інформаційне повідомлення.
     *
     * @param title   Заголовок повідомлення
     * @param message Текст повідомлення
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}