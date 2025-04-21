package medics.medrovarsarm;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditOrderViews implements Initializable {

    @FXML private Label clientLabel;
    @FXML private Label productLabel;
    @FXML private Label availableLabel;
    @FXML private TextField quantityField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private int orderId;
    private int currentQuantity;
    private int productId;
    private int availableQuantity;
    private double productPrice;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveBtn.setOnAction(event -> handleSave());
        cancelBtn.setOnAction(event -> handleCancel());
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadOrderData();
    }

    public void setCurrentQuantity(int quantity) {
        this.currentQuantity = quantity;
    }

    private void loadOrderData() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT o.id_product, c.PIB, p.name_product, p.price, " +
                             "(p.quantity - COALESCE((SELECT SUM(quantity) FROM Orders WHERE id_product = o.id_product), 0)) as available " +
                             "FROM Orders o " +
                             "JOIN Clients c ON o.id_client = c.id " +
                             "JOIN Product p ON o.id_product = p.id " +
                             "WHERE o.id = ?")) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                productId = rs.getInt("id_product");
                clientLabel.setText(rs.getString("PIB"));
                productLabel.setText(rs.getString("name_product"));
                productPrice = rs.getDouble("price");
                availableQuantity = rs.getInt("available") + currentQuantity;
                availableLabel.setText("Доступно: " + availableQuantity);
                quantityField.setText(String.valueOf(currentQuantity));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка", "Не вдалося завантажити дані замовлення");
        }
    }

    private void handleSave() {
        try {
            int newQuantity = Integer.parseInt(quantityField.getText());

            if (newQuantity <= 0) {
                showErrorAlert("Помилка", "Кількість повинна бути більше нуля");
                return;
            }

            if (newQuantity > availableQuantity) {
                showErrorAlert("Помилка", "Недостатня кількість товару на складі");
                return;
            }

            DB db = new DB();
            try (Connection conn = db.getDbConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE Orders SET quantity = ?, total = ? WHERE id = ?")) {

                stmt.setInt(1, newQuantity);
                stmt.setDouble(2, newQuantity * productPrice);
                stmt.setInt(3, orderId);
                stmt.executeUpdate();

                showInfoAlert("Успіх", "Замовлення успішно оновлено");
                closeWindow();
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка", "Будь ласка, введіть коректну кількість");
        } catch (SQLException e) {
            showErrorAlert("Помилка", "Не вдалося оновити замовлення");
        }
    }

    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
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