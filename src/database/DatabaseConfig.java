package database;

import java.io.File;

public class DatabaseConfig {
    // SQLite Database Configuration (fallback for testing)
    private static final String DATABASE_PATH = "resources" + File.separator + "foodstorage.db";
    public static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_PATH;
    
    // For MySQL (when available), uncomment and modify these:
    // public static final String DB_HOST = "localhost";
    // public static final String DB_PORT = "3306";
    // public static final String DB_NAME = "food_storage_db";
    // public static final String DB_USERNAME = "root";
    // public static final String DB_PASSWORD = "your_password_here";
    // public static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + 
    //                                      "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    
    // Table and column names
    public static final String TABLE_FOOD_ITEMS = "food_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_CATEGORY = "category";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_EXPIRY_DATE = "expiry_date";
}
