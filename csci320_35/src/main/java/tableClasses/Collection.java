package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Collection {
    private Connection connection;

    public Collection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates a new collection for a user.
     *
     * @param userId ID of the user creating the collection.
     * @param name Name of the collection.
     * @return true if the collection was created successfully, false otherwise.
     */
    public boolean createCollection(int userId, String name) {
        String sql = "INSERT INTO collection (User_ID, Name) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a collection by its ID.
     *
     * @param collectionId ID of the collection to be deleted.
     * @return true if the collection was deleted successfully, false otherwise.
     */
    public boolean deleteCollection(int collectionId) {
        String sql = "DELETE FROM collection WHERE Collection_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a collection's name by its ID.
     *
     * @param collectionId ID of the collection.
     * @return The name of the collection, or null if not found.
     */
    public String getCollectionName(int collectionId) {
        String sql = "SELECT Name FROM collection WHERE Collection_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}