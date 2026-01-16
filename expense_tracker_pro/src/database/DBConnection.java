
//DBconnection
package database;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/expense_tracker_pro",
                    "root",
                    ""
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
