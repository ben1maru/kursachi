package medics.medrovarsarm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.security.CodeSource;

/**
 * Підключення до SQLite бази даних
 */
public class DB {
    private static Connection dbConnection; // Статичне з'єднання для одноразового використання
    private static final String DB_NAME = "Medics.db";
    private static final String DB_FOLDER = "DB";

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
            // Отримуємо директорію, де розташовано JAR або корінь проєкту
            Path jarDir;
            try {
                CodeSource codeSource = DB.class.getProtectionDomain().getCodeSource();
                if (codeSource != null && codeSource.getLocation() != null) {
                    // Якщо запущено з JAR, отримуємо директорію JAR
                    jarDir = Paths.get(codeSource.getLocation().toURI()).getParent();
                } else {
                    // Якщо запущено в IDE, використовуємо корінь проєкту
                    jarDir = Paths.get(System.getProperty("user.dir"));
                }
            } catch (URISyntaxException e) {
                throw new SQLException("Не вдалося визначити розташування програми: " + e.getMessage());
            }

            // Логування для дебагу
            System.out.println("Робоча директорія: " + jarDir.toAbsolutePath());

            // Шлях до бази даних у папці DB
            Path dbDir = jarDir.resolve(DB_FOLDER);
            Path dbPath = dbDir.resolve(DB_NAME);

            // Перевірка існування файлу бази даних
            if (!Files.exists(dbPath)) {
                throw new SQLException("Файл бази даних не знайдено: " + dbPath.toAbsolutePath() +
                        ". Переконайтеся, що Medics.db розташовано в папці DB у корені проєкту.");
            }

            // Формуємо URL для SQLite
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
}