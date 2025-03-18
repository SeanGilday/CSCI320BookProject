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
     * Marks a book as read by a user, specifying start and end pages.
     *
     * @param startTime The timestamp of when the reading session started.
     * @param userId    The ID of the user reading the book.
     * @param bookId    The ID of the book being read.
     * @param startPage The starting page of reading.
     * @param endPage   The ending page of reading.
     * @param endTime   The timestamp of when the reading session ended.
     */
    public void startReading(Timestamp startTime, int userId, int bookId, int startPage, int endPage,
            Timestamp endTime) {
        String sql = "INSERT INTO user_book_session (Start_Time, User_ID, Book_ID, Start_Page, End_Page, End_Time) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, startTime);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            stmt.setInt(4, startPage);
            stmt.setInt(5, endPage);
            stmt.setTimestamp(6, endTime);
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
     * @return The ID of the book or -1 if no books found in the collection.
     */
    public int startReadingRandomBook(int userId, int collectionId) {
        String sql = "SELECT Book_ID FROM collection_book WHERE Collection_ID = ? ORDER BY RANDOM() LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("Book_ID");
                return bookId;
            } else {
                return -1; // Return -1 if no books found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Return -1 in case of an exception
        }
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
     * Retrieves the book name based on the book ID.
     *
     * @param bookId The ID of the book.
     * @return The name of the book if it exists, otherwise null.
     */
    public String getBookName(int bookId) {
        String sql = "SELECT book_name FROM book WHERE Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("book_name"); // Return the book name if it exists.
            } else {
                return null; // Return null if the book ID does not exist.
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // In case of an error, return null.
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
}
