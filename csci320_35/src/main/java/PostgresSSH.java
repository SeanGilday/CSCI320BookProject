import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresSSH {

    public static Connection conn = null;

    public static void connect() throws SQLException {

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;

        String filePath = "./csci320_35/config/config.txt";
        String user = null, password = null, databaseName = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            user = br.readLine();
            password = br.readLine();
            databaseName = br.readLine();

            String driverName = "org.postgresql.Driver";
            conn = null;
            Session session = null;
            try {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                session = jsch.getSession(user, rhost, 22);
                session.setPassword(password);
                session.setConfig(config);
                session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
                session.connect();
                System.out.println("Connected");
                int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
                System.out.println("Port Forwarded");

                // Assigned port could be different from 5432 but rarely happens
                String url = "jdbc:postgresql://127.0.0.1:" + assigned_port + "/" + databaseName;

                System.out.println("database Url: " + url);
                Properties props = new Properties();
                props.put("user", user);
                props.put("password", password);

                Class.forName(driverName);
                conn = DriverManager.getConnection(url, props);
                System.out.println("Database connection established");

                // Do something with the database....

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("Closing Database Connection");
                    conn.close();
                }
                if (session != null && session.isConnected()) {
                    System.out.println("Closing SSH Connection");
                    session.disconnect();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}