package nl.utwente.mod4.shotmaniacs.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static DatabaseConnection INSTANCE;

    private String host = "bronto.ewi.utwente.nl";
    private String dbName = "dab_di23242b_107";
    private String url = "jdbc:postgresql://" + host + ":5432/" + dbName + "?currentSchema=shotmaniacs";

    private String username = "dab_di23242b_107";
    private String password = "A/anzpGoatCn+lIH";

    public DatabaseConnection() {
        INSTANCE = this;
    }

    public Connection getConnection() {
        try{
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, username, password);
        }
         catch (SQLException sqlError) {
             System.out.println("Cannot connect to database: " + sqlError);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }

        // should never happen
        return null;
    }
}
