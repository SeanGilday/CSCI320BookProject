package tableClasses;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class User {
    private Connection connection;

    public User(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates a new user in the database.
     *
     * @param username  The username of the user.
     * @param password  The password of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     * @param email     The email address of the user.
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
     * Retrieves a user's ID by username.
     *
     * @param username The username of the user.
     * @return The user ID if found, otherwise -1.
     */
    public int getUserId(String username) {
        String sql = "SELECT user_id FROM users WHERE Username = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves a user's ID by email.
     *
     * @param email The email of the user.
     * @return The user ID if found, otherwise -1.
     */
    public int getUserIdFromEmail(String email) {
        String sql = "SELECT user_id FROM users WHERE Email = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Check if a username exists.
     *
     * @param username The username of the user.
     * @return true if username exists, otherwise false.
     */
    public boolean checkUsername(String username) {
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
     * @return true if password is correct, otherwise false.
     */
    public boolean checkPassword(String username, String password) {
        String sql = "SELECT user_id, password FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String storedHash = rs.getString("password");

                // Append user ID to password and hash it using MD5
                String hashedInput = hashPasswordMD5(password + userId);

                // Compare hashes
                return hashedInput.equalsIgnoreCase(storedHash); // Case-insensitive comparison
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Hashes a password using MD5.
     *
     * @param password The password to hash.
     * @return The hashed password as a hex string.
     */
    private String hashPasswordMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Updates the last access date of a user.
     *
     * @param userId The ID of the user.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateLastAccessDate(int userId) {
        String sql = "UPDATE users SET Last_Access_Date = ? WHERE USER_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates all users' passwords by hashing (password + user_id) using MD5.
     */
    public void updatePasswords() {
        String selectSql = "SELECT user_id, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (
                PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                ResultSet rs = selectStmt.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String originalPassword = rs.getString("password");

                // Hash password + user_id
                String hashedPassword = hashPasswordMD5(originalPassword + userId);

                // Update hashed password in database
                updateStmt.setString(1, hashedPassword);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
            }
            System.out.println("Password update completed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
