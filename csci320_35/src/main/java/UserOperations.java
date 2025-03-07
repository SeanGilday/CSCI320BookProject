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
            System.out.println("Username not found. Would you like to create a new account? (1) Yes (2) No");
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
                String password = scanner.nextLine();

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
                "Would you like to\n\t(1) Create a collection\n\t(2) Modify a collection\n\t" +
                        "(3) See all collections\n\t(4) Search for a book\n\t" +
                        "(5) Read a book\n\t(6) Rate a book\n\t" +
                        "(7) Follow another user\n\t(7) Unfollow another user\n\t(9) Exit");
        int mainChoice = getUserChoice(9);
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
        } else {
            System.out.println("Failed to create collection.");
        }
    }

    /**
     * Lists all collections for the current user.
     */
    private static void seeAllCollections() {
        System.out.println("Fetching all collections...");
        List<String> collections = collectionTable.getAllCollections(USER_ID);

        if (collections.isEmpty()) {
            System.out.println("No collections found.");
        } else {
            collections.forEach(System.out::println);
        }
    }

    /**
     * Search method for books in the database.
     */
    private static void searchForBook() {
        System.out.println("Enter book search keyword:");
        String keyword = scanner.nextLine();

        System.out.println(
                "Would you like to search for a book via\n\t(1) Name\n\t(2) Release Date\n\t(3) Author\n\t(4) Publisher\n\t(5) Genre");
        int searchChoice = getUserChoice(5);

        System.out.println(
                "Would you like to sort the list via\n\t(1) Book Name\n\t(2) Publisher\n\t(3) Genre\n\t(4) Released Year");
        int sortChoice = getUserChoice(4);

        System.out.println("Would you like to sort the list via\n\t(1) Ascending\n\t(2) Descending");
        int orderChoice = getUserChoice(2);

        // Determine search field based on user choice
        String searchField;
        switch (searchChoice) {
            case 1 -> searchField = "book_name";
            case 2 -> searchField = "release_date";
            case 3 -> searchField = "author";
            case 4 -> searchField = "publisher";
            case 5 -> searchField = "genre";
            default -> throw new IllegalStateException("Unexpected value: " + searchChoice);
        }

        // Determine sorting field based on user choice
        String sortField;
        switch (sortChoice) {
            case 1 -> sortField = "book_name";
            case 2 -> sortField = "publisher";
            case 3 -> sortField = "genre";
            case 4 -> sortField = "release_year";
            default -> throw new IllegalStateException("Unexpected value: " + sortChoice);
        }

        // Determine sorting order (ascending or descending)
        String order = (orderChoice == 1) ? "ASC" : "DESC";

        // Call database search method
        List<String> books = userBookSessionTable.searchBooks(keyword, searchField, sortField, order);

        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            books.forEach(System.out::println);
        }
    }

    /**
     * Allows user to modify a collection they own either by adding a book, deleting
     * a book, renaming the collection, or deleting the collection.
     */
    private static void modifyCollection() {
        System.out.println("Enter the collection name you want to modify:");
        String collectionName = scanner.nextLine();
        int collectionId = collectionTable.getCollectionId(collectionName);

        if (collectionId == -1) {
            System.out.println("Collection \"" + collectionName + "\" does not exist.");
            return;
        }

        System.out.println(
                "Choose an action:\n\t(1) Add a book\n\t(2) Delete a book\n\t(3) Rename collection\n\t(4) Delete collection\n\t(5) Exit");
        int actionChoice = getUserChoice(5);

        switch (actionChoice) {
            case 1:
                System.out.println("Enter book ID to add:");
                int bookIdToAdd = Integer.parseInt(scanner.nextLine());

                if (!userBookSessionTable.checkBook(bookIdToAdd)) {
                    System.out.println("Book \"" + bookIdToAdd + "\" does not exist.");
                    return;
                }

                collectionBookTable.addCollectionBook(collectionId, bookIdToAdd);
                break;
            case 2:
                System.out.println("Enter book ID to remove:");
                int bookIdToRemove = Integer.parseInt(scanner.nextLine());

                if (!userBookSessionTable.checkBook(bookIdToRemove)) {
                    System.out.println("Book \"" + bookIdToRemove + "\" does not exist.");
                    return;
                }

                collectionBookTable.removeCollectionBook(collectionId, bookIdToRemove);
                break;
            case 3:
                System.out.println("Enter new collection name:");
                String newCollectionName = scanner.nextLine();
                collectionTable.renameCollection(collectionId, newCollectionName);
                break;
            case 4:
                collectionTable.deleteCollection(collectionId);
                break;
            case 5:
                break;
        }
    }

    /**
     * Reads a book by selecting the start and end pages.
     */
    private static void readBook() {
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

        userBookSessionTable.startReading(USER_ID, bookId, startPage, endPage);
        System.out.println("Started reading book from page " + startPage + " to " + endPage + ".");
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
        System.out.println("Enter username to follow:");
        String userToFollow = scanner.nextLine();
        int userIdToFollow = userTable.getUserId(userToFollow);

        if (userIdToFollow == -1) {
            System.out.println("User \"" + userToFollow + "\" does not exist.");
            return;
        }

        userFollowsTable.followUser(USER_ID, userIdToFollow);
        System.out.println("You are now following " + userToFollow);
    }

    /**
     * Allows user to unfollow another user.
     */
    private static void unfollowUser() {
        System.out.println("Enter username to unfollow:");
        String userToUnfollow = scanner.nextLine();
        int userIdToUnfollow = userTable.getUserId(userToUnfollow);

        if (userIdToUnfollow == -1) {
            System.out.println("User \"" + userToUnfollow + "\" does not exist.");
            return;
        }
        // TODO - CHECK IF USER IS ACTUALLY FOLLOWING userToUnfollow

        userFollowsTable.unfollowUser(USER_ID, userIdToUnfollow);
        System.out.println("You have unfollowed " + userToUnfollow);
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
}