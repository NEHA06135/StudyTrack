import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        Connection con = null;

        try {
            // Database URL
            String url = "jdbc:mysql://localhost:3306/studytracker";

            // MySQL username
            String user = "root";

            // MySQL password (put your real password)
            String password = "student06";

            // Create connection
            con = DriverManager.getConnection(url, user, password);

            System.out.println("Connected to MySQL ✅");

        } catch (Exception e) {
            System.out.println("Connection Failed ❌");
            e.printStackTrace();
        }

        return con;
    }
}