import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import tableClasses.Collection;
import tableClasses.CollectionBook;
import tableClasses.User;
import tableClasses.UserBookRating;
import tableClasses.UserBookSession;
import tableClasses.UserCollection;
import tableClasses.UserFollows;

/**
 * This class handles user operations, including authentication,
 * collection management, book searching, reading, rating, and user
 * interactions.
 */
public class UserOperations {

    private static Scanner scanner = new Scanner(System.in);
    private static Collection collectionTable = new Collection(PostgresSSH.conn);
    private static CollectionBook collectionBookTable = new CollectionBook(PostgresSSH.conn);
    private static User userTable = new User(PostgresSSH.conn);
    private static UserBookRating userBookRatingTable = new UserBookRating(PostgresSSH.conn);
    private static UserBookSession userBookSessionTable = new UserBookSession(PostgresSSH.conn);
    private static UserCollection userCollectionTable = new UserCollection(PostgresSSH.conn);
    private static UserFollows userFollowsTable = new UserFollows(PostgresSSH.conn);
    private static int USER_ID = -1;

    /**
     * Starts the user authentication process and application loop.
     */
    public static void start() {
        System.out.println("Welcome to Books.gov");
        while (true) {
            boolean loggedIn = login();
            if (loggedIn) {
                userTable.updateLastAccessDate(USER_ID);
                break;
            }
        }
        while (true) {
            boolean runProgram = showMainMenu();
            if (!runProgram) {
                break;
            }
        }
    }

    /**
     * Handles user login or account creation.
     *
     * @return true if login is successful, false otherwise.
     */
    private static boolean login() {
        System.out.println("Please Login via Username");
        String username = scanner.nextLine();

        // Check if the user exists in the database
        boolean userExists = userTable.checkUsername(username);

        if (!userExists) {
            System.out.println("Username not found. Would you like to create a new account? (1) Yes, (2) No");
            int createChoice = getUserChoice(2);

            if (createChoice == 1) {
                // Create a new user
                System.out.println("Enter Password: ");
                String password = scanner.nextLine();
                System.out.println("Enter First Name: ");
                String firstName = scanner.nextLine();
                System.out.println("Enter Last Name: ");
                String lastName = scanner.nextLine();
                System.out.println("Enter Email: ");
                String email = scanner.nextLine();

                // Call createUser method from User class to add new user to the database
                boolean success = userTable.createUser(username, password, firstName, lastName, email);
                if (success) {
                    System.out.println("User created successfully. You are now logged in.");
                    USER_ID = userTable.getUserId(username);
                    return true;
                } else {
                    System.out.println("Failed to create user. Try again later.");
                    return false;
                }
            } else {
                System.out.println("Returning to main menu.");
                return false;
            }
        } else {
            // User exists, now check the password
            boolean passwordCorrect = false;

            while (!passwordCorrect) {
                System.out.println("Please enter your password or enter \"exit\": ");
                System.out.println("\tNote: The password is hidden. If you type it in wrong it will ask you again.");
                char[] passwordArray = System.console().readPassword(); // Hide password input

                // Convert char[] password to String
                String password = new String(passwordArray);

                // Check if the entered password matches the stored password
                passwordCorrect = userTable.checkPassword(username, password);

                if (passwordCorrect) {
                    System.out.println("Login successful.");
                    USER_ID = userTable.getUserId(username);
                    return true;
                } else if (password.equalsIgnoreCase("exit")) {
                    System.out.println("Returning to main menu.");
                    return false;
                } else {
                    System.out.println("Incorrect password. Please try again.");
                }
            }
        }
        return false;
    }

    /**
     * Displays the main menu and handles user selections.
     *
     * @return false if the user chooses to exit, true otherwise.
     */
    private static boolean showMainMenu() {
        System.out.println(
                "Would you like to\n" +
                        "\t(1) Create a collection\n" +
                        "\t(2) Modify one of your collections\n" +
                        "\t(3) See all your collections\n" +
                        "\t(4) Search for a book\n" +
                        "\t(5) Read a book\n" +
                        "\t(6) Rate a book\n" +
                        "\t(7) Follow another user\n" +
                        "\t(8) Unfollow another user\n" +
                        "\t(9) Display following and followers\n" +
                        "\t(10) Top 20 books read in the past 90 days\n" +
                        "\t(11) Top 20 books among followers\n" +
                        "\t(12) Top 5 books released in this month\n" +
                        "\t(13) Recommend books\n" +
                        "\t(14) Your top 10 books\n" +
                        "\t(15) Exit");
        int mainChoice = getUserChoice(15);
        switch (mainChoice) {
            case 1:
                createCollection();
                return true;
            case 2:
                modifyCollection();
                return true;
            case 3:
                seeAllCollections();
                return true;
            case 4:
                searchForBook();
                return true;
            case 5:
                readBook();
                return true;
            case 6:
                rateBook();
                return true;
            case 7:
                followUser();
                return true;
            case 8:
                unfollowUser();
                return true;
            case 9:
                followingAndFollowers();
                return true;
            case 10:
                top20Books();
                return true;
            case 11:
                top20BooksAmongFollowers();
                return true;
            case 12:
                top5NewReleases();
                return true;
            case 13:
                recommendBooks();
                return true;
            case 14:
                userTop10();
                return true;
            case 15:
                return false;
        }
        return true;
    }

    /**
     * Creates a collection for the current user.
     */
    private static void createCollection() {
        System.out.println("Enter the name of the new collection:");
        String collectionName = scanner.nextLine();

        boolean success = collectionTable.createCollection(collectionName);
        if (success) {
            int collectionId = collectionTable.getCollectionId(collectionName);
            success = success && userCollectionTable.addUserCollection(USER_ID, collectionId);
        }

        if (success) {
            System.out.println("Collection created successfully.");
            System.out.println(
                    "Collection \"" + collectionName + "\" ID is " + collectionTable.getCollectionId(collectionName));
        } else {
            System.out.println("Failed to create collection.");
        }
    }

    /**
     * Lists all collections for the current user.
     */
    private static void seeAllCollections() {
        System.out.println("Fetching all of your collections...");
        List<String> collections = collectionTable.getAllCollections(USER_ID);

        if (collections.isEmpty()) {
            System.out.println("No collections found.");
        } else {
            collections.forEach(System.out::println);
            System.out.println("Number of collections: " + collections.size());
        }
    }

    /**
     * Search method for books in the database.
     */
    private static void searchForBook() {
        System.out.println(
                "Would you like to search for a book via\n\t(1) Name\n\t(2) Release Date\n\t(3) Author\n\t(4) Publisher\n\t(5) Genre");
        int searchChoice = getUserChoice(5);
        if (searchChoice == 2) {
            System.out.println("Date must be in format YYYY-MM-DD HH:MM:SS");
        }

        System.out.println("Enter book search keyword:");
        String keyword = scanner.nextLine();

        if (searchChoice == 2) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date releaseDate = sdf.parse(keyword);
                new Timestamp(releaseDate.getTime());
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again with the format YYYY-MM-DD HH:MM:SS.");
                return;
            }
        }

        // Determine search field based on user choice
        String searchField = switch (searchChoice) {
            case 1 -> "title";
            case 2 -> "release_date";
            case 3 -> "author";
            case 4 -> "publisher";
            case 5 -> "genre";
            default -> throw new IllegalStateException("Unexpected value: " + searchChoice);
        };

        // Fetch books only once
        List<String> books = userBookSessionTable.searchBooks(keyword, searchField);

        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        books.forEach(System.out::println);

        boolean sortAgainCheck = true;
        while (sortAgainCheck) {
            System.out.println("Would you like to sort again? (1) Yes, (2) No");
            int sortAgain = getUserChoice(2);
            if (sortAgain == 2) {
                return;
            }

            System.out.println(
                    "Would you like to sort the list via\n\t(1) Book Name\n\t(2) Publisher\n\t(3) Genre\n\t(4) Released Year");
            int sortChoice = getUserChoice(4);

            System.out.println("Would you like to sort the list via\n\t(1) Ascending\n\t(2) Descending");
            int orderChoice = getUserChoice(2);

            // Sort the books list in-memory
            Comparator<String> comparator = switch (sortChoice) {
                case 1 -> Comparator.comparing(book -> book.split(" \\| ")[1]); // Book Name
                case 2 -> Comparator.comparing(book -> book.split(" \\| ")[3]); // Publisher
                case 3 -> Comparator.comparing(book -> book.split(" \\| ")[7]); // Genre
                case 4 -> Comparator.comparingInt(book -> {
                    String[] parts = book.split(" \\| ");
                    return Integer.parseInt(parts[8].trim());
                }); // Release Year
                default -> throw new IllegalStateException("Unexpected value: " + sortChoice);
            };

            if (orderChoice == 2) {
                comparator = comparator.reversed();
            }

            books.sort(comparator);

            // Display sorted books
            books.forEach(System.out::println);
        }
    }

    /**
     * Allows user to modify a collection they own either by adding a book, deleting
     * a book, renaming the collection, or deleting the collection.
     */
    private static void modifyCollection() {
        System.out.println("Enter the collection ID you want to modify:");
        int collectionId = Integer.parseInt(scanner.nextLine());

        if (collectionId == -1) {
            System.out.println("Collection \"" + collectionId + "\" does not exist.");
            return;
        }

        System.out.println(
                "Choose an action:\n\t(1) Add a book\n\t(2) Remove a book\n\t(3) Rename collection\n\t(4) Delete collection\n\t(5) Exit");
        int actionChoice = getUserChoice(5);

        switch (actionChoice) {
            case 1:
                System.out.println("Enter book ID to add:");
                int bookIdToAdd = Integer.parseInt(scanner.nextLine());

                if (!userBookSessionTable.checkBook(bookIdToAdd)) {
                    System.out.println("Book \"" + bookIdToAdd + "\" does not exist.");
                    return;
                }

                // Check if the book is already in the collection
                if (collectionBookTable.collectionHasBook(collectionId, bookIdToAdd)) {
                    System.out.println("Book is already in the collection.");
                    return;
                }

                collectionBookTable.addCollectionBook(collectionId, bookIdToAdd);
                System.out.println("Book added to collection.");
                break;

            case 2:
                System.out.println("Enter book ID to remove:");
                int bookIdToRemove = Integer.parseInt(scanner.nextLine());

                if (!userBookSessionTable.checkBook(bookIdToRemove)) {
                    System.out.println("Book \"" + bookIdToRemove + "\" does not exist.");
                    return;
                }

                // Check if the book is in the collection before removing
                if (!collectionBookTable.collectionHasBook(collectionId, bookIdToRemove)) {
                    System.out.println("Book is not in the collection.");
                    return;
                }

                collectionBookTable.removeCollectionBook(collectionId, bookIdToRemove);
                System.out.println("Book removed from collection.");
                break;

            case 3:
                System.out.println("Enter new collection name:");
                String newCollectionName = scanner.nextLine();
                collectionTable.renameCollection(collectionId, newCollectionName);
                System.out.println("Collection renamed.");
                break;

            case 4:
                collectionTable.deleteCollection(collectionId);
                userCollectionTable.deleteUserCollection(USER_ID, collectionId);
                System.out.println("Collection deleted.");
                break;

            case 5:
                break;
        }
    }

    /**
     * Reads a book by selecting the start and end pages or reading a random book
     * from a collection.
     */
    private static void readBook() {
        System.out.println("Choose an option:");
        System.out.println("\t(1) Read a specific book");
        System.out.println("\t(2) Read a random book from a collection");

        // Use the getUserChoice method to ensure valid input
        int option = getUserChoice(2);

        if (option == 1) {
            // Read a specific book
            System.out.println("Enter the book ID you want to read:");
            int bookId = Integer.parseInt(scanner.nextLine());

            if (!userBookSessionTable.checkBook(bookId)) {
                System.out.println("Book \"" + bookId + "\" does not exist.");
                return;
            }

            System.out.println("Enter the starting page:");
            int startPage = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter the ending page:");
            int endPage = Integer.parseInt(scanner.nextLine());

            if (endPage < startPage) {
                System.out.println("The ending page cannot be less than the starting page.");
                return;
            }

            // Ask for the start and end times
            System.out.println("Enter the start time (in format YYYY-MM-DD HH:MM:SS):");
            String startTimeStr = scanner.nextLine();

            System.out.println("Enter the end time (in format YYYY-MM-DD HH:MM:SS):");
            String endTimeStr = scanner.nextLine();

            // Convert the start and end times from String to Timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp startTime = null;
            Timestamp endTime = null;
            try {
                Date startDate = sdf.parse(startTimeStr);
                startTime = new Timestamp(startDate.getTime());

                Date endDate = sdf.parse(endTimeStr);
                endTime = new Timestamp(endDate.getTime());
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again with the format YYYY-MM-DD HH:MM:SS.");
                return;
            }

            // Call startReading with all necessary parameters
            userBookSessionTable.startReading(startTime, USER_ID, bookId, startPage, endPage, endTime);
            System.out.println("Started reading book from page " + startPage + " to " + endPage + ".");
        } else if (option == 2) {
            // Read a random book from a collection
            System.out.println("Enter the collection ID to read a random book from:");
            int collectionId = Integer.parseInt(scanner.nextLine());

            // Get the bookId for the random book
            int bookId = userBookSessionTable.startReadingRandomBook(USER_ID, collectionId);

            if (bookId != -1) {
                // Get the book name from bookId
                String bookName = userBookSessionTable.getBookName(bookId);

                // Ask for the start and end pages
                System.out.println("Enter the starting page for the random book:");
                int startPage = Integer.parseInt(scanner.nextLine());

                System.out.println("Enter the ending page for the random book:");
                int endPage = Integer.parseInt(scanner.nextLine());

                if (endPage < startPage) {
                    System.out.println("The ending page cannot be less than the starting page.");
                    return;
                }

                // Ask for the start and end times
                System.out.println("Enter the start time (in format YYYY-MM-DD HH:MM:SS):");
                String startTimeStr = scanner.nextLine();

                System.out.println("Enter the end time (in format YYYY-MM-DD HH:MM:SS):");
                String endTimeStr = scanner.nextLine();

                // Convert the start and end times from String to Timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Timestamp startTime = null;
                Timestamp endTime = null;
                try {
                    Date startDate = sdf.parse(startTimeStr);
                    startTime = new Timestamp(startDate.getTime());

                    Date endDate = sdf.parse(endTimeStr);
                    endTime = new Timestamp(endDate.getTime());
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please try again with the format YYYY-MM-DD HH:MM:SS.");
                    return;
                }

                // If a valid bookId was returned, start reading the book
                userBookSessionTable.startReading(startTime, USER_ID, bookId, startPage, endPage, endTime);
                // Output the book name, book ID, and collection ID
                System.out.println("Started reading a random book from collection " + collectionId + ": " + bookId
                        + " | " + bookName);
            } else {
                System.out.println("No books found in the collection.");
            }
        }
    }

    /**
     * Allows user to rate a book.
     */
    private static void rateBook() {
        System.out.println("Enter book ID to rate:");
        int bookId = Integer.parseInt(scanner.nextLine());

        if (!userBookSessionTable.checkBook(bookId)) {
            System.out.println("Book \"" + bookId + "\" does not exist.");
            return;
        }

        int rating = -1;
        while (rating < 1 || rating > 5) {
            System.out.print("Enter rating (1-" + 5 + "): ");
            try {
                rating = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }

        userBookRatingTable.rateBook(USER_ID, bookId, rating);
        System.out.println("Book rated successfully.");
    }

    /**
     * Allows user to follow another user.
     */
    private static void followUser() {
        System.out.println("Enter email of user to follow:");
        String emailToFollow = scanner.nextLine();
        int userIdToFollow = userTable.getUserIdFromEmail(emailToFollow);

        if (userIdToFollow == -1) {
            System.out.println("User with email \"" + emailToFollow + "\" does not exist.");
            return;
        }

        // Prevent user from following themselves
        if (USER_ID == userIdToFollow) {
            System.out.println("You cannot follow yourself.");
            return;
        }

        // Check if the user is already following the target user
        if (userFollowsTable.isFollowing(USER_ID, userIdToFollow)) {
            System.out.println("You are already following " + emailToFollow + ".");
            return;
        }

        userFollowsTable.followUser(USER_ID, userIdToFollow);
        System.out.println("You are now following " + emailToFollow);
    }

    /**
     * Allows user to unfollow another user.
     */
    private static void unfollowUser() {
        System.out.println("Enter email of user to unfollow:");
        String emailToUnfollow = scanner.nextLine();
        int userIdToUnfollow = userTable.getUserIdFromEmail(emailToUnfollow);

        if (userIdToUnfollow == -1) {
            System.out.println("User with email \"" + emailToUnfollow + "\" does not exist.");
            return;
        }

        // Check if the user is following the user to unfollow
        if (!userFollowsTable.isFollowing(USER_ID, userIdToUnfollow)) {
            System.out.println("You are not following " + emailToUnfollow + ".");
            return;
        }

        // If following, proceed to unfollow
        userFollowsTable.unfollowUser(USER_ID, userIdToUnfollow);
        System.out.println("You have unfollowed " + emailToUnfollow);
    }

    /**
     * Lists the top 20 most popular books among a user's followers.
     */
    private static void top20BooksAmongFollowers() {
        System.out.println("Fetching top 20 books among your followers...");
        List<String> topBooks = userBookSessionTable.getTopBooksAmongFollowers(USER_ID, 20);

        if (topBooks.isEmpty()) {
            System.out.println("No popular books found among your followers.");
        } else {
            topBooks.forEach(System.out::println);
            System.out.println("Total books listed: " + topBooks.size());
        }
    }

    /**
     * Recommends books based on the user's read history and similar users.
     */
    private static void recommendBooks() {
        System.out.println("Fetching 10 recommended books for you...");
        List<String> recommendedBooks = userBookSessionTable.getRecommendedBooks(USER_ID, 10);

        if (recommendedBooks.isEmpty()) {
            System.out.println("No recommendations available at the moment.");
        } else {
            recommendedBooks.forEach(System.out::println);
            System.out.println("Total recommendations: " + recommendedBooks.size());
        }
    }

    /*
     * Prints the user's followers and following
     */
    private static void followingAndFollowers() {
        System.out.println("Fetching following and followers");
        System.out.println("You have " + userFollowsTable.followers(USER_ID) + " followers and "
                + userFollowsTable.following(USER_ID) + " following");
    }

    /*
     * Prints the most read and highly rated books from the past 90 days
     */
    private static void top20Books() {
        System.out.println("Fetching top 20 books...");
        List<String> topBooks = userBookSessionTable.getTop20();

        topBooks.forEach(System.out::println);
        System.out.println("Total books listed: " + topBooks.size());
    }

    /*
     * Prints the most read and highly rated books released the current month
     */
    private static void top5NewReleases() {
        System.out.println("Fetching top 5 new releases...");
        List<String> topBooks = userBookSessionTable.getTop5NewReleases();

        topBooks.forEach(System.out::println);
        System.out.println("Total books listed: " + topBooks.size());
    }

    /**
     * Prompts the user to enter a choice within a given range.
     *
     * @param maxOption The highest option number available.
     * @return The user's selected choice.
     */
    private static int getUserChoice(int maxOption) {
        int choice = -1;
        while (choice < 1 || choice > maxOption) {
            System.out.print("Please enter a choice (1-" + maxOption + "): ");
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
        return choice;
    }

    private static void userTop10() {
        System.out.println("Fetching your top 10 books");
        List<String> topBooks = userBookRatingTable.userTop10(USER_ID);
        int counter = 1;
        for (String book: topBooks) {
            System.out.println(counter + ". " + book);
            counter++;
        }
    }
}