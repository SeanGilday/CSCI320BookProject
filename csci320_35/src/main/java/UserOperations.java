import java.util.Scanner;

public class UserOperations {

    private static Scanner scanner = new Scanner(System.in);

    public static void start() {
        System.out.println("Welcome to Books.gov");
        login();
        showMainMenu();
    }

    private static void login() {
        System.out.println("Please Login via Username");
        String username = scanner.nextLine();
        System.out.println("Please Login via Password");
        String password = scanner.nextLine();
    }

    private static void showMainMenu() {
        System.out.println("Would you like to\n\t(1) Create a collection\n\t(2) See all collections?\n\t(3) Search for a book");
        int mainChoice = getUserChoice(3);
        switch (mainChoice) {
            case 1:
                createCollection();
                break;
            case 2:
                seeAllCollections();
                break;
            case 3:
                searchForBook();
                break;
        }
    }

    private static void createCollection() {
        System.out.println("Creating a collection...");
        // Implementation for creating a collection
        showMainMenu();
    }

    private static void seeAllCollections() {
        System.out.println("Viewing all collections...");
        // Implementation for viewing collections
        showMainMenu();
    }

    private static void searchForBook() {
        System.out.println("Would you like to search for a book via\n\t(1) Name\n\t(2) Release Date\n\t(3) Author\n\t(4) Publisher\n\t(5) Genre");
        int searchChoice = getUserChoice(5);
        System.out.println("Would you like to sort the list via\n\t(1) Book Name\n\t(2) Publisher\n\t(3) Genre\n\t(4) Released Year");
        int sortChoice = getUserChoice(4);
        System.out.println("Would you like to sort the list via\n\t(1) Ascending\n\t(2) Descending");
        int orderChoice = getUserChoice(2);

        // Based on choices, implement actual search and sorting logic

        System.out.println("Searching for books...");
        showMainMenu();
    }

    private static void modifyCollection() {
        System.out.println("Would you like to modify a collection?\n\t(1) Yes\n\t(2) No");
        int modifyChoice = getUserChoice(2);
        if (modifyChoice == 1) {
            System.out.println("Which collection?");
            // Implement logic to select collection
            System.out.println("What would you like to do to this collection?\n\t(1) Add a book\n\t(2) Delete a book\n\t(3) Modify the name\n\t(4) Delete the entire collection");
            int actionChoice = getUserChoice(4);
            // Implement the corresponding action
        }
        showMainMenu();
    }

    private static void readBook() {
        System.out.println("Would you like to read a book?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Reading the book...");
        }
        showMainMenu();
    }

    private static void rateBook() {
        System.out.println("Would you like to rate a book?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Rate the book...");
        }
        showMainMenu();
    }

    private static void followUser() {
        System.out.println("Would you like to follow another user?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Following another user...");
        }
        showMainMenu();
    }

    private static void unfollowUser() {
        System.out.println("Would you like to unfollow another user?\n\t(1) Yes\n\t(2) No");
        int choice = getUserChoice(2);
        if (choice == 1) {
            System.out.println("Unfollowing a user...");
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
