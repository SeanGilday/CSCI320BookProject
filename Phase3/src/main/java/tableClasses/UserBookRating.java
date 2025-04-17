package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserBookRating {
    private Connection connection;

    public UserBookRating(Connection connection) {
        this.connection = connection;
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
     * Rates a book by a user.
     *
     * @param userId The ID of the user rating the book.
     * @param bookId The ID of the book being rated.
     * @param rating The rating given to the book (1-5 stars).
     */
    public void rateBook(int userId, int bookId, int rating) {
        String sql = "INSERT INTO user_book_rating (User_ID, Book_ID, Rating) VALUES (?, ?, ?) " +
                     "ON CONFLICT (User_ID, Book_ID) DO UPDATE SET Rating = EXCLUDED.Rating";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setInt(3, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Returns the current user's top 10 books by rating then number of sessions
     */
    public List<String> userTop10(int userId) {
        List<String> top10 = new ArrayList<>();
        String sql = "SELECT book.title, user_book_rating.rating, " +
                    "COUNT(user_book_session.book_id) AS session_count " +
                    "FROM user_book_rating user_book_rating " +
                    "LEFT JOIN user_book_session user_book_session " +
                    "ON user_book_rating.user_id = user_book_session.user_id " +
                    "AND user_book_rating.book_id = user_book_session.book_id " +
                    "INNER JOIN book book ON user_book_rating.book_id = book.book_id " +
                    "WHERE user_book_rating.user_id = ? " +
                    "GROUP BY user_book_rating.book_id, book.title, user_book_rating.rating " +
                    "ORDER BY user_book_rating.rating DESC, session_count DESC;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String bookID = rs.getString("Title") + " | " + rs.getString("Rating");
                    top10.add(bookID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top10;
    }
}
