package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserBookRating {
    private Connection connection;

    public UserBookRating(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a user rating to a book.
     *
     * @param userId ID of the user.
     * @param bookID ID of the book.
     * @param bookID value of the rating.
     * @return true if the association was created successfully, false otherwise.
     */
    public boolean addUserBookRating(int userId, int bookId, int rating) {
        String sql = "INSERT INTO user_book_rating (User_ID, Collection_ID, Rating) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setInt(3, rating);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates a user's rating for a specific book.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @param rating New rating value.
     * @return true if the rating was updated successfully, false otherwise.
     */
    public boolean updateUserBookRating(int userId, int bookId, int rating) {
        String sql = "UPDATE user_book_rating SET Rating = ? WHERE User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user's rating for a specific book.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @return true if the rating was deleted successfully, false otherwise.
     */
    public boolean deleteUserBookRating(int userId, int bookId) {
        String sql = "DELETE FROM user_book_rating WHERE User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the rating given by a user to a specific book.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @return the rating, or -1 if no rating exists.
     */
    public int getUserBookRating(int userId, int bookId) {
        String sql = "SELECT Rating FROM user_book_rating WHERE User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Rating");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Checks if a user has rated a specific book.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @return true if the user has rated the book, false otherwise.
     */
    public boolean userHasRatedBook(int userId, int bookId) {
        String sql = "SELECT 1 FROM user_book_rating WHERE User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
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
