package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserCollection {
    private Connection connection;

    public UserCollection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a collection to a user.
     *
     * @param userId ID of the user.
     * @param collectionId ID of the collection.
     * @return true if the association was created successfully, false otherwise.
     */
    public boolean addUserCollection(int userId, int collectionId) {
        String sql = "INSERT INTO user_collection (User_ID, Collection_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, collectionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete collection from a user.
     *
     * @param userId ID of the user.
     * @param collectionId ID of the collection.
     * @return true if the association was removed successfully, false otherwise.
     */
    public boolean deleteUserCollection(int userId, int collectionId) {
        String sql = "DELETE FROM user_collection WHERE User_ID = ? AND Collection_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, collectionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
