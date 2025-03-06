import java.util.Scanner;

import tableClasses.Collection;
import tableClasses.CollectionBook;
import tableClasses.User;
import tableClasses.UserBookRating;
import tableClasses.UserBookSession;
import tableClasses.UserCollection;
import tableClasses.UserFollows;

public class UserOperations {

    private static Scanner scanner = new Scanner(System.in);
    private static Collection collectionTable = new Collection(PostgresSSH.conn);
    private static CollectionBook collectionBookTable = new CollectionBook(PostgresSSH.conn);
    private static User userTable = new User(PostgresSSH.conn);
    private static UserBookRating userBookRatingTable = new UserBookRating(PostgresSSH.conn);
    private static UserBookSession userBookSessionTable = new UserBookSession(PostgresSSH.conn);
    private static UserCollection userCollectionTable = new UserCollection(PostgresSSH.conn);
    private static UserFollows userFollowsTable = new UserFollows(PostgresSSH.conn);

    private static String USERNAME = null;

    public static void start() {
        System.out.println("Welcome to Books.gov");
        while (true) {
            boolean loggedIn = login();
            if (loggedIn) {
                userTable.updateLastAccessDate(USERNAME);
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

    private static boolean login() {
        System.out.println("Please Login via Username");
        String username = scanner.nextLine();

        // Check if the user exists in the database
        boolean userExists = userTable.getUser(username);

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
                    USERNAME = username;
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
                    USERNAME = username;
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

    private static boolean showMainMenu() {
        System.out.println(
                "Would you like to\n\t(1) Create a collection\n\t(2) See all collections?\n\t(3) Search for a book\n\t(4) Exit");
        int mainChoice = getUserChoice(4);
        switch (mainChoice) {
            case 1:
                createCollection();
                return true;
            case 2:
                seeAllCollections();
                return true;
            case 3:
                searchForBook();
                return true;
            case 4:
                return false;
        }
        return true;
    }

    private static void createCollection() {
        System.out.println("Creating a collection...");
        // TODO - Implementation for creating a collection
        showMainMenu();
    }

    private static void seeAllCollections() {
        System.out.println("Viewing all collections...");
        // TODO - Implementation for viewing collections
        showMainMenu();
    }

    private static void searchForBook() {
        System.out.println(
                "Would you like to search for a book via\n\t(1) Name\n\t(2) Release Date\n\t(3) Author\n\t(4) Publisher\n\t(5) Genre");
        int searchChoice = getUserChoice(5);
        System.out.println(
                "Would you like to sort the list via\n\t(1) Book Name\n\t(2) Publisher\n\t(3) Genre\n\t(4) Released Year");
        int sortChoice = getUserChoice(4);
        System.out.println("Would you like to sort the list via\n\t(1) Ascending\n\t(2) Descending");
        int orderChoice = getUserChoice(2);

        // TODO - Based on choices, implement actual search and sorting logic

        System.out.println("Searching for books...");
        showMainMenu();
    }

    private static void modifyCollection() {
        System.out.println("Would you like to modify a collection?\n\t(1) Yes\n\t(2) No");
        int modifyChoice = getUserChoice(2);
        if (modifyChoice == 1) {
            System.out.println("Which collection?");
            // TODO - Implement logic to select collection
            System.out.println(
                    "What would you like to do to this collection?\n\t(1) Add a book\n\t(2) Delete a book\n\t(3) Modify the name\n\t(4) Delete the entire collection");
            int actionChoice = getUserChoice(4);
            // TODO - Implement the corresponding action
        }
        showMainMenu();
    }

    private static void readBook() {
        System.out.println("Would you like to read a book?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Reading the book...");
            // TODO
        }
        showMainMenu();
    }

    private static void rateBook() {
        System.out.println("Would you like to rate a book?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Rate the book...");
            // TODO
        }
        showMainMenu();
    }

    private static void followUser() {
        System.out.println("Would you like to follow another user?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Following another user...");
            // TODO
        }
        showMainMenu();
    }

    private static void unfollowUser() {
        System.out.println("Would you like to unfollow another user?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Unfollowing a user...");
            // TODO
        }
        showMainMenu();
    }

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
