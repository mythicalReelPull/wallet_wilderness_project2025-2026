package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // This creates a file named 'wallet.db' in your project folder
    private static final String URL = "jdbc:sqlite:wallet.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {

        // 1. Define the SQL strings (Keep these at the top as you had them)
        String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL" +
                ");";

        String profileTable = "CREATE TABLE IF NOT EXISTS profiles (" +
                "id INTEGER PRIMARY KEY," +
                "userId INTEGER," +
                "name TEXT NOT NULL," +
                "FOREIGN KEY(userId) REFERENCES users(id)" +
                ");";

        String transactionTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "profile_id INTEGER, " +
                "amount REAL, " +
                "category TEXT, " +
                "description TEXT, " +
                "FOREIGN KEY(profile_id) REFERENCES profiles(id))";

        // 2. The try-with-resources block creates 'stmt'
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 3. Move all executions here so they can see the 'stmt' variable
            stmt.execute(userTable);
            stmt.execute(profileTable);
            stmt.execute(transactionTable); // Fixed: Moved inside the block

            System.out.println("Database tables initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}