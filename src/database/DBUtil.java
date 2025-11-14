package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBUtil {
    private DBUtil() {}

    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
        
        // Ensure resources directory exists
        ensureResourcesDirectoryExists();
        
        // Create the table
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_FOOD_ITEMS + " (" +
                            DatabaseConfig.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            DatabaseConfig.COL_NAME + " TEXT NOT NULL, " +
                            DatabaseConfig.COL_CATEGORY + " TEXT, " +
                            DatabaseConfig.COL_QUANTITY + " INTEGER DEFAULT 0, " +
                            DatabaseConfig.COL_EXPIRY_DATE + " TEXT" +
                            ")"
            );

            // Users table: username (PK), role (NGO or SUPPLIER)
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "role TEXT NOT NULL CHECK(role IN ('NGO','SUPPLIER'))" +
                ")"
            );

            // Orders table
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ngo_username TEXT NOT NULL, " +
                "item_id INTEGER NOT NULL, " +
                "item_name TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "order_date TEXT NOT NULL, " +
                "FOREIGN KEY (ngo_username) REFERENCES users(username) ON DELETE CASCADE" +
                ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database table", e);
        }
    }
    
    private static void ensureResourcesDirectoryExists() {
        java.io.File resourcesDir = new java.io.File("resources");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.JDBC_URL);
    }

    public static ResultSet selectAllFoodItems(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "SELECT " + DatabaseConfig.COL_ID + ", " + DatabaseConfig.COL_NAME + ", " + 
            DatabaseConfig.COL_CATEGORY + ", " + DatabaseConfig.COL_QUANTITY + ", " + 
            DatabaseConfig.COL_EXPIRY_DATE + " FROM " + DatabaseConfig.TABLE_FOOD_ITEMS
        );
        return statement.executeQuery();
    }

    public static void insertFoodItem(Connection connection, String name, String category, int quantity, String expiryDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + DatabaseConfig.TABLE_FOOD_ITEMS + "(" + 
                DatabaseConfig.COL_NAME + ", " + DatabaseConfig.COL_CATEGORY + ", " + 
                DatabaseConfig.COL_QUANTITY + ", " + DatabaseConfig.COL_EXPIRY_DATE + ") VALUES (?, ?, ?, ?)")) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setInt(3, quantity);
            statement.setString(4, expiryDate);
            statement.executeUpdate();
        }
    }

    public static void deleteFoodItemById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + DatabaseConfig.TABLE_FOOD_ITEMS + " WHERE " + DatabaseConfig.COL_ID + " = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
    
    public static void updateFoodItem(Connection connection, int id, String name, String category, int quantity, String expiryDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + DatabaseConfig.TABLE_FOOD_ITEMS + " SET " + 
                DatabaseConfig.COL_NAME + " = ?, " + DatabaseConfig.COL_CATEGORY + " = ?, " + 
                DatabaseConfig.COL_QUANTITY + " = ?, " + DatabaseConfig.COL_EXPIRY_DATE + " = ? WHERE " + 
                DatabaseConfig.COL_ID + " = ?")) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setInt(3, quantity);
            statement.setString(4, expiryDate);
            statement.setInt(5, id);
            statement.executeUpdate();
        }
    }
    
    public static ResultSet searchFoodItems(Connection connection, String searchTerm) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
            "SELECT " + DatabaseConfig.COL_ID + ", " + DatabaseConfig.COL_NAME + ", " + 
            DatabaseConfig.COL_CATEGORY + ", " + DatabaseConfig.COL_QUANTITY + ", " + 
            DatabaseConfig.COL_EXPIRY_DATE + " FROM " + DatabaseConfig.TABLE_FOOD_ITEMS + 
            " WHERE " + DatabaseConfig.COL_NAME + " LIKE ? OR " + DatabaseConfig.COL_CATEGORY + " LIKE ?"
        );
        String searchPattern = "%" + searchTerm + "%";
        statement.setString(1, searchPattern);
        statement.setString(2, searchPattern);
        return statement.executeQuery();
    }

    // Users
    public static void ensureUser(String username, String role) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO users(username, role) VALUES(?, ?) ON CONFLICT(username) DO UPDATE SET role=excluded.role")) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.executeUpdate();
        }
    }

    public static String getUserRole(String username) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT role FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
                return null;
            }
        }
    }

    // Orders and stock adjustments
    public static void insertOrderAndDecrementStock(String ngoUsername, int itemId, String itemName, int qty, String orderDate) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement orderPs = connection.prepareStatement(
                        "INSERT INTO orders(ngo_username, item_id, item_name, quantity, order_date) VALUES(?,?,?,?,?)");
                 PreparedStatement stockPs = connection.prepareStatement(
                        "UPDATE " + DatabaseConfig.TABLE_FOOD_ITEMS + " SET " + DatabaseConfig.COL_QUANTITY + " = " + DatabaseConfig.COL_QUANTITY + " - ? WHERE " + DatabaseConfig.COL_ID + " = ? AND " + DatabaseConfig.COL_QUANTITY + " >= ?");) {
                orderPs.setString(1, ngoUsername);
                orderPs.setInt(2, itemId);
                orderPs.setString(3, itemName);
                orderPs.setInt(4, qty);
                orderPs.setString(5, orderDate);
                orderPs.executeUpdate();

                stockPs.setInt(1, qty);
                stockPs.setInt(2, itemId);
                stockPs.setInt(3, qty);
                int updated = stockPs.executeUpdate();
                if (updated == 0) {
                    connection.rollback();
                    throw new SQLException("Insufficient stock or item not found");
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static ResultSet selectOrdersForNgo(Connection connection, String ngoUsername) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT id, ngo_username, item_id, item_name, quantity, order_date FROM orders WHERE ngo_username = ? ORDER BY id DESC");
        ps.setString(1, ngoUsername);
        return ps.executeQuery();
    }

    public static ResultSet selectAllOrders(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT id, ngo_username, item_id, item_name, quantity, order_date FROM orders ORDER BY id DESC");
        return ps.executeQuery();
    }

}



