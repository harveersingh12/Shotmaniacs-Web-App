package dao;

import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    @BeforeAll
    public static void init() {
        new DatabaseConnection();
    }

    @Test
    public void testDBConnection() {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try(connection) {
            String query = "SELECT * FROM event";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            assertTrue(resultSet.next(), "Query should return at least one row");
        }
        catch (SQLException e) {
            fail("Error connecting to database" + e.getMessage());
        }
    }
}
