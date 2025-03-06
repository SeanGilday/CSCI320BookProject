package tableClasses;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private Connection connection;

    public User(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates a new user in the database.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email address of the user.
     * @return true if the user was successfully created, false otherwise.
     */
    public boolean createUser(String username, String password, String firstName, String lastName, String email) {
        String sql = "INSERT INTO users (Username, Password, First_Name, Last_Name, Email, Creation_Date, Last_Access_Date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, email);
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a user's information by username.
     *
     * @param username The username of the user.
     * @return A ResultSet containing the user details, or null if not found.
     */
    public boolean getUser(String username) {
        String sql = "SELECT 1 FROM users WHERE Username = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a user entered their password correctly.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A ResultSet containing the user details, or null if not found.
     */
    public boolean checkPassword(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE Username = ? AND Password = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the last access date of a user.
     *
     * @param userId The ID of the user.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateLastAccessDate(String username) {
        String sql = "UPDATE users SET Last_Access_Date = ? WHERE Username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
