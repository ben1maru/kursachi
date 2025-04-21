package medics.medrovarsarm;

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

public class ProductsViews {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button AddCategory;
    @FXML private Button AddClient;
    @FXML private Button AddProducts;
    @FXML private Button BackBtn;
    @FXML private TableColumn<Product, String> CategoryProducts;
    @FXML private TableColumn<Product, String> CountryCrated;
    @FXML private TableColumn<Product, String> NameProducts;
    @FXML private TableColumn<Product, Double> PriceProducts;
    @FXML private TableColumn<Product, Integer> QuantityColumn;
    @FXML private TextField SearchField;
    @FXML private TableColumn<Product, String> StatusColumn;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Void> actionColumns;
    private ObservableList<Product> productsData = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        AddProducts.setOnAction(event -> handleAddProduct());
        BackBtn.setOnAction(event -> handleBack());
        AddCategory.setOnAction(event -> handleAddCategory());
        AddClient.setOnAction(event -> handleAddClient());

        try {
            configureTableColumns();
            loadProductsData();
            setupSearch();
            setupRowHighlighting();
        } catch (Exception e) {
            showErrorAlert("Помилка ініціалізації", e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureTableColumns() {
        NameProducts.setCellValueFactory(new PropertyValueFactory<>("name"));
        CategoryProducts.setCellValueFactory(new PropertyValueFactory<>("category"));
        CountryCrated.setCellValueFactory(new PropertyValueFactory<>("country"));
        PriceProducts.setCellValueFactory(new PropertyValueFactory<>("price"));
        QuantityColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumns.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<Product, Void>, TableCell<Product, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button issueBtn = new Button("Видати");
                    private final Button editBtn = new Button("Редагувати");
                    private final Button deleteBtn = new Button("Видалити");
                    private final HBox hbox = new HBox(5, issueBtn, editBtn, deleteBtn);

                    {
                        issueBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-family: 'Roboto';");
                        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-family: 'Roboto';");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-family: 'Roboto';");

                        issueBtn.setOnAction(event -> {
                            int productId = getTableView().getItems().get(getIndex()).getId();
                            handleIssueProduct(productId);
                        });

                        editBtn.setOnAction(event -> {
                            int productId = getTableView().getItems().get(getIndex()).getId();
                            handleEditProduct(productId);
                        });

                        deleteBtn.setOnAction(event -> {
                            int productId = getTableView().getItems().get(getIndex()).getId();
                            handleDeleteProduct(productId);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Product product = getTableView().getItems().get(getIndex());
                            issueBtn.setDisable(product.getAvailableQuantity() <= 0);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        };
    }

    private void handleIssueProduct(int productId) {
        try {
            Product product = getProductById(productId);
            if (product == null) {
                showErrorAlert("Помилка", "Продукт не знайдено");
                return;
            }

            URL fxmlLocation = getClass().getResource("IssueProductViews.fxml");
            if (fxmlLocation == null) {
                showErrorAlert("Помилка", "Не вдалося знайти IssueProductViews.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            IssueProductViews controller = loader.getController();
            if (controller == null) {
                showErrorAlert("Помилка", "Контролер IssueProductViews не ініціалізований");
                return;
            }

            controller.setProductId(productId);
            controller.setAvailableQuantity(product.getAvailableQuantity());
            controller.setProductPrice(product.getPrice());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProductsData();
        } catch (Exception e) {
            showErrorAlert("Помилка", "Не вдалося відкрити форму видачі товару: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditProduct(int productId) {
        try {
            URL fxmlLocation = getClass().getResource("EditProductViews.fxml");
            if (fxmlLocation == null) {
                showErrorAlert("Помилка", "Не вдалося знайти EditProductViews.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            EditProductViews controller = loader.getController();
            if (controller == null) {
                showErrorAlert("Помилка", "Контролер EditProductViews не ініціалізований");
                return;
            }

            controller.setProductId(productId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProductsData();
        } catch (Exception e) {
            showErrorAlert("Помилка", "Не вдалося відкрити форму редагування товару: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteProduct(int productId) {
        Product product = getProductById(productId);
        if (product == null) {
            showErrorAlert("Помилка", "Продукт не знайдено");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Підтвердження видалення");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Ви впевнені, що хочете видалити продукт '" + product.getName() + "'?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            DB db = new DB();
            try (Connection conn = db.getDbConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM Product WHERE id = ?")) {

                stmt.setInt(1, productId);
                stmt.executeUpdate();
                loadProductsData();
            } catch (SQLException e) {
                showErrorAlert("Помилка видалення", "Не вдалося видалити продукт: " + e.getMessage());
            }
        }
    }

    private Product getProductById(int productId) {
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id, p.name_product, c.name as category_name, p.country, " +
                             "p.price, p.quantity as total_quantity, s.name as status_name, " +
                             "(SELECT COALESCE(SUM(o.quantity), 0) FROM Orders o WHERE o.id_product = p.id) as ordered_quantity " +
                             "FROM Product p " +
                             "JOIN category c ON p.id_category = c.id " +
                             "JOIN status s ON p.id_status = s.id " +
                             "WHERE p.id = ?")) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int totalQuantity = rs.getInt("total_quantity");
                int orderedQuantity = rs.getInt("ordered_quantity");
                int availableQuantity = totalQuantity - orderedQuantity;

                return new Product(
                        rs.getInt("id"),
                        rs.getString("name_product"),
                        rs.getString("category_name"),
                        rs.getString("country"),
                        rs.getDouble("price"),
                        totalQuantity,
                        availableQuantity,
                        rs.getString("status_name")
                );
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка завантаження даних", e.getMessage());
        }
        return null;
    }

    private synchronized void loadProductsData() {
        productsData.clear();
        DB db = new DB();
        try (Connection conn = db.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id, p.name_product, c.name as category_name, p.country, " +
                             "p.price, p.quantity as total_quantity, s.name as status_name, " +
                             "(SELECT COALESCE(SUM(o.quantity), 0) FROM Orders o WHERE o.id_product = p.id) as ordered_quantity " +
                             "FROM Product p " +
                             "JOIN category c ON p.id_category = c.id " +
                             "JOIN status s ON p.id_status = s.id");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int totalQuantity = rs.getInt("total_quantity");
                int orderedQuantity = rs.getInt("ordered_quantity");
                int availableQuantity = totalQuantity - orderedQuantity;

                productsData.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name_product"),
                        rs.getString("category_name"),
                        rs.getString("country"),
                        rs.getDouble("price"),
                        totalQuantity,
                        availableQuantity,
                        rs.getString("status_name")
                ));
            }
        } catch (SQLException e) {
            showErrorAlert("Помилка завантаження даних", e.getMessage());
        }
    }

    private void setupRowHighlighting() {
        productsTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (product == null || empty) {
                    setStyle("");
                } else {
                    setStyle(product.getAvailableQuantity() <= 0 ? "-fx-background-color: #ffcccc;" : "");
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Product> filteredData = new FilteredList<>(productsData, p -> true);

        SearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return product.getName().toLowerCase().contains(lowerCaseFilter) ||
                        product.getCategory().toLowerCase().contains(lowerCaseFilter) ||
                        product.getCountry().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(product.getPrice()).contains(lowerCaseFilter) ||
                        String.valueOf(product.getAvailableQuantity()).contains(lowerCaseFilter);
            });
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productsTable.comparatorProperty());
        productsTable.setItems(sortedData);
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddProductViews.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProductsData();
        } catch (Exception e) {
            showErrorAlert("Помилка додавання товару", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainViews.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) BackBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            showErrorAlert("Помилка повернення", e.getMessage());
        }
    }

    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddCategoryViews.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProductsData();
        } catch (Exception e) {
            showErrorAlert("Помилка додавання категорії", e.getMessage());
        }
    }

    @FXML
    private void handleAddClient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddClientViews.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProductsData();
        } catch (Exception e) {
            showErrorAlert("Помилка додавання клієнта", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Product {
        private final int id;
        private final String name;
        private final String category;
        private final String country;
        private final double price;
        private final int totalQuantity;
        private final int availableQuantity;
        private final String status;

        public Product(int id, String name, String category, String country, double price,
                       int totalQuantity, int availableQuantity, String status) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.country = country;
            this.price = price;
            this.totalQuantity = totalQuantity;
            this.availableQuantity = availableQuantity;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getCountry() { return country; }
        public double getPrice() { return price; }
        public int getTotalQuantity() { return totalQuantity; }
        public int getAvailableQuantity() { return availableQuantity; }
        public String getStatus() { return status; }
    }
}