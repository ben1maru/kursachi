package biblioty.armbiblioty;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditBookTheyTookViews {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField clientField;

    @FXML
    private TextField dateField;

    @FXML
    private Button saveBtn;

    private int recordId;
    private int bookId;

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    @FXML
    void initialize() {
        loadRecordData();
        saveBtn.setOnAction(event -> saveChanges());
    }

    private void loadRecordData() {
        DB db = new DB();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getDbConnection();
            String query = "SELECT name_clients, date_the_took FROM booktheytook WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, recordId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                clientField.setText(rs.getString("name_clients"));
                dateField.setText(rs.getString("date_the_took"));
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося завантажити дані: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // З'єднання не закриваємо
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void saveChanges() {
        if (clientField.getText().trim().isEmpty() || dateField.getText().trim().isEmpty()) {
            showAlert("Попередження", "Заповніть усі поля", Alert.AlertType.WARNING);
            return;
        }

        DB db = new DB();
        Connection conn = null;

        try {
            conn = db.getDbConnection();
            String query = "UPDATE booktheytook SET name_clients = ?, date_the_took = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, clientField.getText().trim());
                stmt.setString(2, dateField.getText().trim());
                stmt.setInt(3, recordId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert("Успіх", "Запис оновлено", Alert.AlertType.INFORMATION);
                    closeWindow();
                }
            }
        } catch (SQLException e) {
            showAlert("Помилка", "Не вдалося оновити запис: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
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