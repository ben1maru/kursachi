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
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class IssueProductViews implements Initializable {

    @FXML private ChoiceBox<String> clientChoiceBox;
    @FXML private TextField quantityField;
    @FXML private Label productNameLabel;
    @FXML private Label availableQuantityLabel;
    @FXML private Label priceLabel;
    @FXML private Label totalLabel;
    @FXML private Button issueButton;
    @FXML private Button cancelButton;

    private ObservableList<String> clients = FXCollections.observableArrayList();
    private int productId;
    private int availableQuantity;
    private double productPrice;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadClients();

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateTotal();
        });

        issueButton.setOnAction(event -> handleIssue());
        cancelButton.setOnAction(event -> handleCancel());
    }

    public void setProductId(int productId) {
        this.productId = productId;
        loadProductInfo();
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
        availableQuantityLabel.setText(String.valueOf(availableQuantity));
    }

    public void setProductPrice(double price) {
        this.productPrice = price;
        priceLabel.setText(String.format("%.2f грн", price));
    }

    private void loadProductInfo() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name_product FROM Product WHERE id = ?")) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                productNameLabel.setText(rs.getString("name_product"));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка", "Не вдалося завантажити інформацію про продукт");
        }
    }

    private void loadClients() {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT PIB FROM Clients ORDER BY PIB");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add(rs.getString("PIB"));
            }
            clientChoiceBox.setItems(clients);
        } catch (SQLException e) {
            showErrorAlert("Помилка", "Не вдалося завантажити список клієнтів");
        }
    }

    private void calculateTotal() {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            double total = quantity * productPrice;
            totalLabel.setText(String.format("%.2f грн", total));
        } catch (NumberFormatException e) {
            totalLabel.setText("0.00 грн");
        }
    }

    private void handleIssue() {
        if (validateInput()) {
            try {
                int quantity = Integer.parseInt(quantityField.getText());

                if (quantity > availableQuantity) {
                    showErrorAlert("Помилка", "Недостатня кількість товару на складі");
                    return;
                }

                DB db = new DB();
                try (Connection conn = db.getDbConnection()) {
                    conn.setAutoCommit(false);

                    // Отримуємо ID клієнта
                    int clientId;
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT id FROM Clients WHERE PIB = ?")) {

                        stmt.setString(1, clientChoiceBox.getValue());
                        ResultSet rs = stmt.executeQuery();
                        clientId = rs.next() ? rs.getInt("id") : -1;
                    }

                    if (clientId == -1) {
                        showErrorAlert("Помилка", "Обраний клієнт не знайдений");
                        return;
                    }

                    // Додаємо запис про видачу
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO Orders (id_client, id_product, quantity, total) VALUES (?, ?, ?, ?)")) {

                        stmt.setInt(1, clientId);
                        stmt.setInt(2, productId);
                        stmt.setInt(3, quantity);
                        stmt.setDouble(4, quantity * productPrice);
                        stmt.executeUpdate();
                    }

                    conn.commit();
                    showInfoAlert("Успіх", "Товар успішно видано клієнту");
                    handleCancel();
                }
            } catch (SQLException e) {
                showErrorAlert("Помилка бази даних", "Не вдалося оформити видачу: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        if (clientChoiceBox.getValue() == null) {
            showErrorAlert("Помилка", "Будь ласка, оберіть клієнта");
            return false;
        }

        if (quantityField.getText().isEmpty()) {
            showErrorAlert("Помилка", "Будь ласка, введіть кількість");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                showErrorAlert("Помилка", "Кількість повинна бути більше нуля");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Помилка", "Кількість повинна бути цілим числом");
            return false;
        }

        return true;
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