package medics.medrovarsarm;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
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

public class OrderViews {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> clientColumn;
    @FXML private TableColumn<Order, String> productColumn;
    @FXML private TableColumn<Order, Integer> quantityColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Button backBtn;

    private ObservableList<Order> ordersData = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        configureTableColumns();
        loadOrdersData();
        setupSearch();

        backBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainViews.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) backBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                showErrorAlert("Помилка", "Не вдалося повернутися до головного меню");
            }
        });
    }

    private void configureTableColumns() {
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        actionColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<Order, Void>, TableCell<Order, Void>> createActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button editBtn = new Button("Редагувати");
            private final Button deleteBtn = new Button("Видалити");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleEditOrder(order);
                });

                deleteBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleDeleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        };
    }

    private void handleEditOrder(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditOrderViews.fxml"));
            Parent root = loader.load();

            EditOrderViews controller = loader.getController();
            controller.setOrderId(order.getId());
            controller.setCurrentQuantity(order.getQuantity());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadOrdersData();
        } catch (IOException e) {
            showErrorAlert("Помилка", "Не вдалося відкрити форму редагування");
        }
    }

    private void handleDeleteOrder(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Підтвердження видалення");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Ви впевнені, що хочете видалити це замовлення?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            DB db = new DB();
            try (Connection conn = db.getDbConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM Orders WHERE id = ?")) {

                stmt.setInt(1, order.getId());
                stmt.executeUpdate();

                showInfoAlert("Успіх", "Замовлення успішно видалено");
                loadOrdersData();
            } catch (SQLException e) {
                showErrorAlert("Помилка", "Не вдалося видалити замовлення");
            }
        }
    }

    private void loadOrdersData() {
        ordersData.clear();
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT o.id, c.PIB as client_name, p.name_product as product_name, " +
                             "o.quantity, o.total " +
                             "FROM Orders o " +
                             "JOIN Clients c ON o.id_client = c.id " +
                             "JOIN Product p ON o.id_product = p.id ");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ordersData.add(new Order(
                        rs.getInt("id"),
                        rs.getString("client_name"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка", "Не вдалося завантажити дані замовлень");
        }
    }

    private void setupSearch() {
        FilteredList<Order> filteredData = new FilteredList<>(ordersData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return order.getProductName().toLowerCase().contains(lowerCaseFilter) ||
                        order.getClientName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Order> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(ordersTable.comparatorProperty());
        ordersTable.setItems(sortedData);
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

    public static class Order {
        private final int id;
        private final String clientName;
        private final String productName;
        private final int quantity;
        private final double total;

        public Order(int id, String clientName, String productName, int quantity,
                     double total) {
            this.id = id;
            this.clientName = clientName;
            this.productName = productName;
            this.quantity = quantity;
            this.total = total;
        }

        public int getId() { return id; }
        public String getClientName() { return clientName; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return total; }
    }
}