package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserBookSession {
    private Connection connection;

    public UserBookSession(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new user book session.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @param startTime Timestamp of when the session starts.
     * @param endTime Timestamp of when the session ends.
     * @param startPage The page the user starts reading from.
     * @param endPage The page the user finishes reading.
     * @return true if the session was added successfully, false otherwise.
     */
    public boolean addUserBookSession(int userId, int bookId, Timestamp startTime, Timestamp endTime, int startPage, int endPage) {
        String sql = "INSERT INTO user_book_session (Start_Time, User_ID, Book_ID, End_Time, Start_Page, End_Page) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, startTime);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            stmt.setTimestamp(4, endTime);
            stmt.setInt(5, startPage);
            stmt.setInt(6, endPage);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the end time and pages of an existing session.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @param startTime Timestamp of when the session started.
     * @param endTime Timestamp of when the session ends.
     * @param startPage The page the user started reading from.
     * @param endPage The page the user finishes reading.
     * @return true if the session was updated successfully, false otherwise.
     */
    public boolean updateUserBookSession(int userId, int bookId, Timestamp startTime, Timestamp endTime, int startPage, int endPage) {
        String sql = "UPDATE user_book_session SET End_Time = ?, Start_Page = ?, End_Page = ? WHERE Start_Time = ? AND User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, endTime);
            stmt.setInt(2, startPage);
            stmt.setInt(3, endPage);
            stmt.setTimestamp(4, startTime);
            stmt.setInt(5, userId);
            stmt.setInt(6, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user book session.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @param startTime Timestamp of when the session started.
     * @return true if the session was deleted successfully, false otherwise.
     */
    public boolean deleteUserBookSession(int userId, int bookId, Timestamp startTime) {
        String sql = "DELETE FROM user_book_session WHERE Start_Time = ? AND User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, startTime);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the session details for a user and a book.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @param startTime Timestamp of when the session started.
     * @return the session details as a result set or null if no session exists.
     */
    public ResultSet getUserBookSession(int userId, int bookId, Timestamp startTime) {
        String sql = "SELECT * FROM user_book_session WHERE Start_Time = ? AND User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, startTime);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a session exists for a user and a book.
     *
     * @param userId ID of the user.
     * @param bookId ID of the book.
     * @param startTime Timestamp of when the session started.
     * @return true if the session exists, false otherwise.
     */
    public boolean userHasBookSession(int userId, int bookId, Timestamp startTime) {
        String sql = "SELECT 1 FROM user_book_session WHERE Start_Time = ? AND User_ID = ? AND Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, startTime);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
