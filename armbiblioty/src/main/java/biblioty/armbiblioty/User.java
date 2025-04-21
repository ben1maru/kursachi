package biblioty.armbiblioty;


/**
 * Клас з юзерами
 */
public class User {

    private int isAdmin;
    private int userId;
    private String nameUser;
    private String password;
    private String  email;

    /**
     * Конструктор юзер для реєстрації
     *
     * @param userId
     * @param nameUser
     * @param email
     * @param password
     * @param isAdmin
     */
    public User(int userId, String nameUser, String email, String password, int isAdmin) {
        this.userId = userId;
        this.nameUser = nameUser;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;

    }

    /**
     * Конструктор юзер для авторизації
     * @param nameUser
     * @param email
     * @param password
     */
    public User(String nameUser, String email, String password) {
        this.nameUser = nameUser;
        this.password = password;
        this.email = email;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    public User() {}

    public String getNameUser() {
        return nameUser;
    }

    public int getUserId() {
        return userId;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}