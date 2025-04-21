package medics.medrovarsarm;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditProductViews {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField nameField;
    @FXML private TextField countryField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ChoiceBox<String> categoryChoiceBox;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;


    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<String> statuses = FXCollections.observableArrayList();
    private int productId;
    private int availableQuantity;
    private double productPrice;

    @FXML
    void initialize() {
        loadCategories();
        loadStatuses();

        categoryChoiceBox.setItems(categories);
        statusChoiceBox.setItems(statuses);

        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());

    }

    public void setProductId(int productId) {
        this.productId = productId;
        loadProductData();
    }

    private void loadProductData() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.name_product, p.country, p.price, p.quantity, c.name AS category, s.name AS status, " +
                             "(p.quantity - COALESCE((SELECT SUM(quantity) FROM Orders WHERE id_product = p.id), 0)) AS available " +
                             "FROM Product p " +
                             "JOIN category c ON p.id_category = c.id " +
                             "JOIN status s ON p.id_status = s.id " +
                             "WHERE p.id = ?")) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name_product"));
                countryField.setText(rs.getString("country"));
                priceField.setText(String.valueOf(rs.getDouble("price")));
                quantityField.setText(String.valueOf(rs.getInt("quantity")));
                categoryChoiceBox.setValue(rs.getString("category"));
                statusChoiceBox.setValue(rs.getString("status"));
                availableQuantity = rs.getInt("available");
                productPrice = rs.getDouble("price");

            }
        } catch (SQLException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося завантажити дані продукту: " + e.getMessage());
        }
    }


    private void loadCategories() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM category");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося завантажити категорії: " + e.getMessage());
        }
    }

    private void loadStatuses() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM status");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statuses.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося завантажити статуси: " + e.getMessage());
        }
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String country = countryField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String category = categoryChoiceBox.getValue();
        String status = statusChoiceBox.getValue();

        if (name.isEmpty() || country.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() ||
                category == null || status == null) {
            showErrorAlert("Помилка", "Усі поля обов'язкові для заповнення");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
            if (price < 0 || quantity < 0) {
                showErrorAlert("Помилка", "Ціна та кількість не можуть бути від'ємними");
                return;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка", "Ціна та кількість повинні бути коректними числами");
            return;
        }

        DB db = new DB();
        try (Connection conn = db.getDbConnection()) {
            conn.setAutoCommit(false);

            // Отримуємо ID категорії
            int categoryId;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM category WHERE name = ?")) {
                stmt.setString(1, category);
                ResultSet rs = stmt.executeQuery();
                categoryId = rs.next() ? rs.getInt("id") : -1;
            }

            // Отримуємо ID статусу
            int statusId;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM status WHERE name = ?")) {
                stmt.setString(1, status);
                ResultSet rs = stmt.executeQuery();
                statusId = rs.next() ? rs.getInt("id") : -1;
            }

            if (categoryId == -1 || statusId == -1) {
                showErrorAlert("Помилка", "Некоректна категорія або статус");
                return;
            }

            // Оновлюємо продукт
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Product SET name_product = ?, country = ?, price = ?, quantity = ?, " +
                            "id_status = ?, id_category = ? WHERE id = ?")) {

                stmt.setString(1, name);
                stmt.setString(2, country);
                stmt.setDouble(3, price);
                stmt.setInt(4, quantity);
                stmt.setInt(5, statusId);
                stmt.setInt(6, categoryId);
                stmt.setInt(7, productId);
                stmt.executeUpdate();
            }

            conn.commit();
            showInfoAlert("Успіх", "Продукт успішно оновлено");
            handleCancel();
        } catch (SQLException e) {
            showErrorAlert("Помилка бази даних", "Не вдалося оновити продукт: " + e.getMessage());
        }
    }

    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
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
}