package medics.medrovarsarm;

import javafx.scene.control.Alert;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * Клас реєстрації та авторизації
 */
public class Autorize {

    DB db = new DB();
    /**
     * Реєстраці користувача
     * @param user
     */
    public void signUpUser(User user) {
        String insert = "INSERT INTO " + Const.GTU_TABLE + "(" +
                Const.GTU_USERNAME + "," + Const.GTU_MAIL + "," +
                Const.GTU_PASSWORD + "," +"isAdmin"+")" + "VALUES(?,?,?,?)";

        try {
            PreparedStatement prSt = db.getDbConnection().prepareStatement(insert);
            prSt.setString(1, user.getNameUser());
            prSt.setString(2, user.getEmail());
            prSt.setString(3, DigestUtils.sha1Hex(user.getPassword()));
            prSt.setInt(4, 0);

            prSt.executeUpdate();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Успіх");
            alert.setContentText("Дані було успішно додано");
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Помилка");
            alert.setContentText("Такий користувач уже існує");
            alert.show();
        }
        return;
    }

    /**
     * Авторизація користувача
     * @param email
     * @param password
     * @return
     */
    public User getUser(String email, String password) {
        User user = null;
        String hashedPassword = DigestUtils.sha1Hex(password);
        String sqlStatement = String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?", Const.GTU_TABLE, Const.GTU_MAIL, Const.GTU_PASSWORD);

        try {
            PreparedStatement preparedStatement = db.getDbConnection().prepareStatement(sqlStatement);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, hashedPassword);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String userName = resultSet.getString("username");
                String loginFromDatabase = resultSet.getString("email");
                String passwordFromDatabase = resultSet.getString("password");
                int isAdmin = resultSet.getInt("isAdmin");

                user = new User(userId, userName, loginFromDatabase, passwordFromDatabase,isAdmin);
                System.out.println(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

}
