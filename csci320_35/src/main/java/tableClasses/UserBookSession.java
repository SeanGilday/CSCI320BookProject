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
        String sql = "SELECT title FROM book WHERE Book_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("title"); // Return the book name if it exists.
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
     * @param keyword     The keyword to search for (e.g., book title, author,
     *                    genre, etc.).
     * @param searchField The field to search in (title, author, publisher, genre,
     *                    etc.).
     * @param sortField   The field to sort by (title, publisher, genre,
     *                    release_year).
     * @param order       The sorting order (ASC or DESC).
     * @return A list of book details matching the criteria.
     */
    public List<String> searchBooks(String keyword, String searchField, String sortField, String order) {
        List<String> books = new ArrayList<>();

        // Start base query with release_year extracted from release_date
        String sql = "SELECT DISTINCT b.book_id, b.title, " +
                "STRING_AGG(DISTINCT CONCAT(a.first_name, ' ', a.last_name), ', ') AS authors, " + // Combine multiple authors
                "p.name AS publisher, " +
                "b.length, " +
                "b.audience, " +
                "COALESCE(AVG(ubr.rating), 0) AS avg_rating, " + // Average rating (default to 0 if no rating)
                "EXTRACT(YEAR FROM b.release_date) AS release_year ";

        // Include genre type in the select list if we're dealing with genre
        if ("genre".equals(searchField) || "genre".equals(sortField)) {
            sql += ", g.genre_type "; // Add genre type to SELECT for ordering
        }

        sql += "FROM book b " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.person_id = a.person_id " +
                "LEFT JOIN book_publisher bp ON b.book_id = bp.book_id " +
                "LEFT JOIN publisher p ON bp.publisher_id = p.publisher_id " +
                "LEFT JOIN user_book_rating ubr ON b.book_id = ubr.book_id ";
        ;

        // Adjust WHERE clause based on search field
        if ("author".equals(searchField)) {
            searchField = "CONCAT(a.first_name, ' ', a.last_name)";
        } else if ("publisher".equals(searchField)) {
            searchField = "p.name";
        } else if ("genre".equals(searchField) || "genre".equals(sortField)) {
            sql += "JOIN book_genre bg ON b.book_id = bg.book_id " +
                    "JOIN genre g ON bg.genre_id = g.genre_id "; // Ensure proper join with genre table
            searchField = "g.genre_type"; // Correct column for genre
        } else if ("release_date".equals(searchField)) {
            searchField = "CAST(b.release_date AS TEXT)";
        }
        if ("genre".equals(searchField)) {
            searchField = "g.genre_type"; // Correct column for genre
        }

        sql += "WHERE " + searchField + " ILIKE ? " +
                "GROUP BY b.book_id, b.title, p.name, b.length, b.audience, b.release_date ";

        // Add g.genre_type to GROUP BY if sorting or searching by genre
        if ("genre".equals(sortField)) {
            sql += ", g.genre_type "; // Add genre to GROUP BY for sorting
        }

        // Handle sorting
        if ("release_year".equals(sortField)) {
            sql += "ORDER BY EXTRACT(YEAR FROM b.release_date) ";
        } else if ("genre".equals(sortField)) {
            sql += "ORDER BY g.genre_type ";
        } else {
            sql += "ORDER BY " + sortField + " ";
        }

        sql += order; // Ascending or descending order

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%"); // Secure input to prevent SQL injection
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookEntry = rs.getInt("book_id") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("authors") + " | " +
                        rs.getString("publisher") + " | " +
                        rs.getInt("length") + " pages | " +
                        rs.getString("audience") + " | " +
                        String.format("%.2f", rs.getDouble("avg_rating")); // Format rating to 2 decimal places
                books.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

}
