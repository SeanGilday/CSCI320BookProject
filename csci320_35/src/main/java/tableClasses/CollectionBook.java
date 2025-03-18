package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CollectionBook {
    private Connection connection;

    public CollectionBook(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a book to a collection.
     *
     * @param collectionId ID of the collection.
     * @param bookId ID of the book.
     * @return true if the association was created successfully, false otherwise.
     */
    public boolean addCollectionBook(int collectionId, int bookId) {
        String sql = "INSERT INTO collection_book (Collection_ID, Book_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a book to a collection.
     *
     * @param bookId ID of the collection.
     * @param bookId ID of the book.
     * @return true if the association was removed successfully, false otherwise.
     */
    public boolean removeCollectionBook(int collectionId, int bookId) {
        String sql = "DELETE FROM collection_book WHERE Collection_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a collection has a book.
     *
     * @param bookId ID of the collection.
     * @param bookId ID of the book.
     * @return true if the user has the collection, false otherwise.
     */
    public boolean collectionHasBook(int collectionId, int bookId) {
        String sql = "SELECT 1 FROM collection_book WHERE Collection_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

