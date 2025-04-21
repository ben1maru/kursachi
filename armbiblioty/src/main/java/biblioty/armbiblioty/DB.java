package biblioty.armbiblioty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Підключення до SQLite бази даних
 */
public class DB {
    private static Connection dbConnection; // Статичне з'єднання для одноразового використання

    static {
        try {
            // Ініціалізація з'єднання при завантаженні класу
            initializeConnection();
        } catch (SQLException e) {
            System.err.println("Помилка ініціалізації з'єднання: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeConnection() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            // Шлях до бази даних у папці resources/DB
            Path dbPath = Paths.get("src", "main", "resources", "DB", "biblioteka.db");
            String url = "jdbc:sqlite:" + dbPath.toString() + "?busy_timeout=5000";

            dbConnection = DriverManager.getConnection(url);
            System.out.println("Підключення до SQLite встановлено: " + dbConnection);

            // Увімкнення підтримки foreign keys
            dbConnection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
    }

    /**
     * Повертає з'єднання з базою даних (завжди одне і те ж)
     * @return об'єкт Connection
     * @throws SQLException
     */
    public Connection getDbConnection() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            initializeConnection(); // Переініціалізація, якщо з'єднання втрачено
        }
        return dbConnection;
    }

    // Метод closeConnection видалено, оскільки з'єднання не закриватиметься
}