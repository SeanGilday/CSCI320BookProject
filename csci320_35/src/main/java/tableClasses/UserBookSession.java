package tableClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserBookSession {
    private Connection connection;

    public UserBookSession(Connection connection) {
        this.connection = connection;
    }

    /**
     * Checks if a book ID exists.
     *
     * @param bookId The id of the book.
     * @return true if book id exists, otherwise false.
     */
    public boolean checkBook(int bookId) {
        String sql = "SELECT book_id FROM book WHERE Book_ID = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, bookId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a new user book session.
     *
     * @param userId    ID of the user.
     * @param bookId    ID of the book.
     * @param startTime Timestamp of when the session starts.
     * @param endTime   Timestamp of when the session ends.
     * @param startPage The page the user starts reading from.
     * @param endPage   The page the user finishes reading.
     * @return true if the session was added successfully, false otherwise.
     */
    public boolean addUserBookSession(int userId, int bookId, Timestamp startTime, Timestamp endTime, int startPage,
            int endPage) {
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
     * @param userId    ID of the user.
     * @param bookId    ID of the book.
     * @param startTime Timestamp of when the session started.
     * @param endTime   Timestamp of when the session ends.
     * @param startPage The page the user started reading from.
     * @param endPage   The page the user finishes reading.
     * @return true if the session was updated successfully, false otherwise.
     */
    public boolean updateUserBookSession(int userId, int bookId, Timestamp startTime, Timestamp endTime, int startPage,
            int endPage) {
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
     * @param userId    ID of the user.
     * @param bookId    ID of the book.
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
     * @param userId    ID of the user.
     * @param bookId    ID of the book.
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
     * @param userId    ID of the user.
     * @param bookId    ID of the book.
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

    /**
     * Searches for books based on a keyword, search field, sort field, and order.
     *
     * @param keyword     The keyword to search for.
     * @param searchField The database column to search in.
     * @param sortField   The database column to sort by.
     * @param order       The sorting order (ASC or DESC).
     * @return A list of book details matching the criteria.
     */
    public List<String> searchBooks(String keyword, String searchField, String sortField, String order) {
        List<String> books = new ArrayList<>();

        String sql = "SELECT book_id, book_name, author, publisher, release_year " +
                "FROM book " +
                "WHERE " + searchField + " ILIKE ? " + // ILIKE for case-insensitive search
                "ORDER BY " + sortField + " " + order;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bookEntry = rs.getInt("book_id") + " | " +
                        rs.getString("book_name") + " | " +
                        rs.getString("author") + " | " +
                        rs.getString("publisher") + " | " +
                        rs.getInt("release_year");
                books.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Marks a book as read by a user, specifying start and end pages.
     *
     * @param userId    The ID of the user reading the book.
     * @param bookId    The ID of the book being read.
     * @param startPage The starting page of reading.
     * @param endPage   The ending page of reading.
     */
    public void startReading(int userId, int bookId, int startPage, int endPage) {
        String sql = "INSERT INTO user_book_session (User_ID, Book_ID, Start_Page, End_Page, Read_Timestamp) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setInt(3, startPage);
            stmt.setInt(4, endPage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks a random book from a user's collection as read.
     *
     * @param userId       The ID of the user.
     * @param collectionId The ID of the collection.
     */
    public void startReadingRandomBook(int userId, int collectionId) {
        String sql = "SELECT Book_ID FROM collection_book WHERE Collection_ID = ? ORDER BY RANDOM() LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("Book_ID");
                startReading(userId, bookId, 1, 100); // Assuming default start and end pages
                System.out.println("Started reading a random book from collection: " + bookId);
            } else {
                System.out.println("No books found in the collection.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
