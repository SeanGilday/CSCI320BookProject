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
    public List<String> searchBooks(String keyword, String searchField) {
        List<String> books = new ArrayList<>();

        // Start base query with release_year extracted from release_date
        String sql = "SELECT DISTINCT b.book_id, b.title, " +
                "STRING_AGG(DISTINCT CONCAT(a.first_name, ' ', a.last_name), ', ') AS authors, " +
                "p.name AS publisher, " +
                "b.length, " +
                "b.audience, " +
                "COALESCE(AVG(ubr.rating), 0) AS avg_rating, " +
                "EXTRACT(YEAR FROM b.release_date) AS release_year, " +
                "COALESCE(STRING_AGG(DISTINCT g.genre_type, ', '), 'Unknown') AS genre ";

        sql += "FROM book b " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.person_id = a.person_id " +
                "LEFT JOIN book_publisher bp ON b.book_id = bp.book_id " +
                "LEFT JOIN publisher p ON bp.publisher_id = p.publisher_id " +
                "LEFT JOIN user_book_rating ubr ON b.book_id = ubr.book_id " +
                "LEFT JOIN book_genre bg ON b.book_id = bg.book_id " +
                "LEFT JOIN genre g ON bg.genre_id = g.genre_id ";

        // Adjust WHERE clause based on search field
        if ("author".equals(searchField)) {
            searchField = "CONCAT(a.first_name, ' ', a.last_name)";
        } else if ("publisher".equals(searchField)) {
            searchField = "p.name";
        } else if ("genre".equals(searchField)) {
            searchField = "g.genre_type";
        } else if ("release_date".equals(searchField)) {
            searchField = "CAST(b.release_date AS TEXT)";
        }

        sql += "WHERE " + searchField + " ILIKE ? " +
                "GROUP BY b.book_id, b.title, p.name, b.length, b.audience, b.release_date " +
                "ORDER BY b.title, EXTRACT(YEAR FROM b.release_date) ";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookEntry = rs.getInt("book_id") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("authors") + " | " +
                        rs.getString("publisher") + " | " +
                        rs.getInt("length") + " pages | " +
                        rs.getString("audience") + " | " +
                        String.format("%.2f", rs.getDouble("avg_rating")) + " | " +
                        rs.getString("genre") + " | " +
                        rs.getInt("release_year");
                books.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Retrieves the top books read by a user's followers based on average rating.
     *
     * @param userId The ID of the user.
     * @param limit  The maximum number of books to retrieve.
     * @return A list of book details.
     */
    public List<String> top20BooksAmongUsersFollowed(int userId, int limit) {
        String sql = "SELECT ubr.Book_ID, b.title, " +
                "STRING_AGG(DISTINCT CONCAT(a.first_name, ' ', a.last_name), ', ') AS authors, " +
                "p.name AS publisher, " +
                "b.length, " +
                "b.audience, " +
                "COALESCE(AVG(ubr.Rating), 0) AS avg_rating, " +
                "EXTRACT(YEAR FROM b.release_date) AS release_year, " +
                "COALESCE(STRING_AGG(DISTINCT g.genre_type, ', '), 'Unknown') AS genre " +
                "FROM user_book_rating ubr " +
                "JOIN user_follows uf ON ubr.User_ID = uf.User_Followed " +
                "JOIN book b ON ubr.Book_ID = b.book_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.person_id = a.person_id " +
                "LEFT JOIN book_publisher bp ON b.book_id = bp.book_id " +
                "LEFT JOIN publisher p ON bp.publisher_id = p.publisher_id " +
                "LEFT JOIN book_genre bg ON b.book_id = bg.book_id " +
                "LEFT JOIN genre g ON bg.genre_id = g.genre_id " +
                "WHERE uf.User_Follower = ? " +
                "GROUP BY ubr.Book_ID, b.title, p.name, b.length, b.audience, b.release_date " +
                "ORDER BY avg_rating DESC " +
                "LIMIT ?";

        List<String> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookEntry = rs.getInt("Book_ID") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("authors") + " | " +
                        rs.getString("publisher") + " | " +
                        rs.getInt("length") + " pages | " +
                        rs.getString("audience") + " | " +
                        String.format("%.2f", rs.getDouble("avg_rating")) + " | " +
                        rs.getString("genre") + " | " +
                        rs.getInt("release_year");
                books.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /**
     * Provides book recommendations based on a user's read history and similar
     * users using weighted similarity scores.
     *
     * @param userId The ID of the user.
     * @param limit  The number of recommendations.
     * @return A list of recommended book details.
     */
    public List<String> getRecommendedBooks(int userId, int limit) {

        /*
         * UserPreferences - Identify genre and book_author with user_book_rating_id
         * 
         * SimilarUsers - Find users who have read books with the same genres or authors
         * 
         * RankedUsers - Assign weight similarity scores to users in the following
         * format:
         * Genre matches (*2)
         * Author matches (*2)
         * Book matches (*1)
         * Highly rated books (*3, since a positive rating matters more)
         * 
         * Retrieve book details based off highest weight that the user has not already read
         */
        String sql = """
                    WITH UserPreferences AS (
                        SELECT bg.genre_id, ba.person_id AS author_id
                        FROM user_book_rating ubr
                        JOIN book_genre bg ON ubr.book_id = bg.book_id
                        JOIN book_author ba ON ubr.book_id = ba.book_id
                        WHERE ubr.user_id = ?
                        GROUP BY bg.genre_id, ba.person_id
                    ),
                    SimilarUsers AS (
                        SELECT ubr.user_id,
                               COUNT(DISTINCT bg.genre_id) AS genre_match_count,
                               COUNT(DISTINCT ba.person_id) AS author_match_count,
                               COUNT(DISTINCT ubr.book_id) AS book_match_count,
                               COUNT(DISTINCT CASE WHEN ubr.rating >= 4 THEN ubr.book_id END) AS high_rating_match
                        FROM user_book_rating ubr
                        JOIN book_genre bg ON ubr.book_id = bg.book_id
                        JOIN book_author ba ON ubr.book_id = ba.book_id
                        WHERE (bg.genre_id IN (SELECT genre_id FROM UserPreferences)
                           OR ba.person_id IN (SELECT author_id FROM UserPreferences))
                          AND ubr.user_id <> ?
                        GROUP BY ubr.user_id
                    ),
                    RankedUsers AS (
                        SELECT user_id,
                               (genre_match_count * 2 + author_match_count * 2 + book_match_count + high_rating_match * 3) AS similarity_score
                        FROM SimilarUsers
                        ORDER BY similarity_score DESC
                        LIMIT 10
                    )
                    SELECT b.book_id, b.title,
                           STRING_AGG(DISTINCT CONCAT(a.first_name, ' ', a.last_name), ', ') AS authors,
                           p.name AS publisher, b.length, b.audience,
                           COALESCE(AVG(ubr.rating), 0) AS avg_rating,
                           EXTRACT(YEAR FROM b.release_date) AS release_year,
                           COALESCE(STRING_AGG(DISTINCT g.genre_type, ', '), 'Unknown') AS genre
                    FROM user_book_rating ubr
                    JOIN book b ON ubr.book_id = b.book_id
                    LEFT JOIN book_author ba ON b.book_id = ba.book_id
                    LEFT JOIN author a ON ba.person_id = a.person_id
                    LEFT JOIN book_publisher bp ON b.book_id = bp.book_id
                    LEFT JOIN publisher p ON bp.publisher_id = p.publisher_id
                    LEFT JOIN book_genre bg ON b.book_id = bg.book_id
                    LEFT JOIN genre g ON bg.genre_id = g.genre_id
                    WHERE ubr.user_id IN (SELECT user_id FROM RankedUsers)
                      AND ubr.book_id NOT IN (SELECT book_id FROM user_book_rating WHERE user_id = ?)
                    GROUP BY b.book_id, b.title, p.name, b.length, b.audience, b.release_date
                    ORDER BY avg_rating DESC
                    LIMIT ?;
                """;

        List<String> recommendations = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookEntry = rs.getInt("book_id") + " | " +
                        rs.getString("title");
                recommendations.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recommendations;
    }

    public List<String> getTop20() {
        /*
         * session_stats - Get number of reading sessions for each book
         * rating_stats - Gets average ratings for books
         * 
         * normalized - Normalizes reading session counts and average ratings
         * 
         * composite_score - 0-1 score calculated from normalized scores using the weights
         *      60% average rating 40% number of sessions
         * 
         * Retrieve book details based off highest composite score calculated
         */
        String sql = """
                    WITH session_stats AS (
                        SELECT
                            Book_ID,
                            COUNT(*) AS reading_session_count
                        FROM
                            User_Book_Session
                        WHERE
                            Start_Time >= CURRENT_DATE - INTERVAL '90 days'
                        GROUP BY
                            Book_ID
                    ),
                    rating_stats AS (
                        SELECT
                            Book_ID,
                            AVG(Rating) AS average_rating
                        FROM
                            User_Book_Rating
                        GROUP BY
                            Book_ID
                    ),
                    combined AS (
                        SELECT
                            ss.Book_ID,
                            ss.reading_session_count,
                            rs.average_rating
                        FROM
                            session_stats ss
                        JOIN
                            rating_stats rs ON ss.Book_ID = rs.Book_ID
                    ),
                    normalized AS (
                        SELECT
                            *,
                            (reading_session_count * 1.0 - MIN(reading_session_count) OVER ()) /
                            NULLIF((MAX(reading_session_count) OVER () - MIN(reading_session_count) OVER ()), 0)
                            AS normalized_sessions,

                            (average_rating - MIN(average_rating) OVER ()) /
                            NULLIF((MAX(average_rating) OVER () - MIN(average_rating) OVER ()), 0)
                            AS normalized_rating
                        FROM
                            combined
                    )
                    SELECT
                        n.Book_ID,
                        b.Title,
                        n.reading_session_count,
                        n.average_rating,
                        ROUND(0.6 * normalized_rating + 0.4 * normalized_sessions, 4) AS composite_score
                    FROM
                        normalized n
                    JOIN
                        Book b ON n.Book_ID = b.Book_ID
                    ORDER BY
                        composite_score DESC
                    LIMIT 20;
                    """;

        List<String> top20 = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookEntry = rs.getInt("Book_ID") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("reading_session_count") + " | " +
                        rs.getString("average_rating");
                top20.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top20;
    }

    public List<String> getTop5NewReleases() {
        String sql = """
                    WITH new_releases AS (
                        SELECT *
                        FROM Book
                        WHERE DATE_TRUNC('month', Release_Date) = DATE_TRUNC('month', CURRENT_DATE)
                    ),
                    book_reads AS (
                        SELECT
                            Book_ID,
                            COUNT(*) AS total_reads
                        FROM User_Book_Session
                        GROUP BY Book_ID
                    ),
                    book_ratings AS (
                        SELECT
                            Book_ID,
                            AVG(Rating) AS avg_rating,
                            COUNT(*) AS rating_count
                        FROM User_Book_Rating
                        GROUP BY Book_ID
                    ),
                    combined AS (
                        SELECT
                            nr.Book_ID,
                            nr.Title,
                            nr.release_date,
                            COALESCE(br.total_reads, 0) AS total_reads,
                            COALESCE(r.avg_rating, 0) AS avg_rating,
                            COALESCE(r.rating_count, 0) AS rating_count
                        FROM new_releases nr
                        LEFT JOIN book_reads br ON nr.Book_ID = br.Book_ID
                        LEFT JOIN book_ratings r ON nr.Book_ID = r.Book_ID
                    ),
                    normalized AS (
                        SELECT
                            *,
                            (total_reads * 1.0 - MIN(total_reads) OVER ()) /
                            NULLIF((MAX(total_reads) OVER () - MIN(total_reads) OVER ()), 0)
                            AS normalized_sessions,

                            (avg_rating - MIN(avg_rating) OVER ()) /
                            NULLIF((MAX(avg_rating) OVER () - MIN(avg_rating) OVER ()), 0)
                            AS normalized_rating
                        FROM
                            combined
                    )
                    SELECT
                        n.Book_ID,
                        b.Title,
                        n.release_date,
                        n.avg_rating,
                        ROUND(0.6 * normalized_rating + 0.4 * normalized_sessions, 4) AS composite_score
                    FROM
                        normalized n
                    JOIN
                        Book b ON n.Book_ID = b.Book_ID
                    ORDER BY
                        composite_score DESC
                    LIMIT 5;
                    """;

        List<String> top5 = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String bookEntry = rs.getInt("Book_ID") + " | " +
                        rs.getString("title") + " | " +
                        rs.getString("release_date") + " | " +
                        rs.getString("avg_rating");
                top5.add(bookEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top5;
    }
}