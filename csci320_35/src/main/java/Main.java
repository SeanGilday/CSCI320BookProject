import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("CSCI320_35 Project Loaded");
        try {
            PostgresSSH.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("CSCI320_35 Project Closed");
        }
    }
}