package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Collection {
    private Connection connection;

    public Collection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates a new collection.
     *
     * @param name   Name of the collection.
     * @return true if the collection was created successfully, false otherwise.
     */
    public boolean createCollection(String name) {
        String sql = "INSERT INTO collection (Name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a collection by its ID.
     *
     * @param collectionId ID of the collection.
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

    /**
     * Retrieves all collections from the database with additional details.
     *
     * @return A List containing collection details, or an empty list if an error
     *         occurs.
     */
    public List<String> getAllCollections(int userId) {
        List<String> collections = new ArrayList<>();
        String sql = "SELECT c.Collection_ID, c.Name, COUNT(cb.Book_ID) AS Book_Count, COALESCE(SUM(b.Length), 0) AS Total_Pages " +
                "FROM collection c " +
                "LEFT JOIN collection_book cb ON c.Collection_ID = cb.Collection_ID " +
                "LEFT JOIN book b ON cb.Book_ID = b.Book_ID " +
                "LEFT JOIN user_collection uc ON c.Collection_ID = uc.Collection_ID " +
                "WHERE uc.User_ID = ? " +
                "GROUP BY c.Collection_ID, c.Name " +
                "ORDER BY c.Collection_ID";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                collections.add(rs.getInt("Collection_ID") + " - " + rs.getString("Name") +
                        " | Books: " + rs.getInt("Book_Count") +
                        " | Total Pages: " + rs.getInt("Total_Pages"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return collections;
    }

    /**
     * Renames a collection in the database.
     *
     * @param collectionId      The ID of the collection to rename.
     * @param newCollectionName The new name for the collection.
     */
    public void renameCollection(int collectionId, String newCollectionName) {
        String sql = "UPDATE collection SET Name = ? WHERE Collection_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newCollectionName);
            stmt.setInt(2, collectionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the ID of a collection based on its name.
     *
     * @param collectionName The name of the collection.
     * @return The ID of the collection, or -1 if not found.
     */
    public int getCollectionId(String collectionName) {
        String sql = "SELECT Collection_ID FROM collection WHERE Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectionName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Collection_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}