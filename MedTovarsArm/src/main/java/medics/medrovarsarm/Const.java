package medics.medrovarsarm;

/**
 * Клас, що містить константи для роботи з базою даних і об'єкт користувача.
 */
public class Const {
    /** Назва таблиці користувачів */
    public static final String GTU_TABLE = "users";
    /** Назва стовпця з ідентифікатором користувача */
    public static final String GTU_ID = "id";
    /** Назва стовпця з ім'ям користувача */
    public static final String GTU_USERNAME = "username";
    /** Назва стовпця з паролем користувача */
    public static final String GTU_PASSWORD = "`password`";
    /** Назва стовпця з електронною поштою користувача */
    public static final String GTU_MAIL = "`email`";

    /** Об'єкт користувача, що зберігає дані авторизованого користувача */
    public static User user = null;
}
