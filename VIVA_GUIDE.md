# Food Storage Management System - Viva Guide

## Overview
**Food Storage Management System** is a JavaFX-based desktop application that manages food inventory with two different user roles: **NGO** (Non-Governmental Organization) and **SUPPLIER**. It uses SQLite database for persistent data storage and enables real-time inventory management with expiry tracking.

---

## 🏗️ Architecture & Workflow

### System Architecture Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                    JavaFX GUI Layer                         │
│  (Login Screen → NGO/Supplier Dashboard)                    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Controller Layer (MVC)                      │
│  LoginController, NgoController, SupplierController         │
│           (Handles user interactions)                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Business Logic Layer                        │
│  FoodItem, User, Order, Session (Model Classes)             │
│           (Data representation)                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              Database Access Layer (DBUtil)                 │
│  JDBC Connection, SQL Queries, Database Operations          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│           SQLite Database (foodstorage.db)                  │
│  Tables: food_items, users, orders                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Application Flow

### 1. **Application Startup Flow**
```
Main.java starts
    ↓
Main.start() method called
    ↓
DBUtil.initializeDatabase() - Creates/initializes tables
    ↓
Loads Login.fxml (Login Screen)
    ↓
Shows JavaFX Stage with Login UI
```

### 2. **User Login & Role Selection Flow**
```
User enters username & selects role (NGO/SUPPLIER)
    ↓
LoginController.handleLogin() called
    ↓
DBUtil.ensureUser(username, role) - Adds user to database if new
    ↓
Session.setCurrentUser() - Stores current user in memory
    ↓
If NGO → Load NgoHome.fxml
If SUPPLIER → Load SupplierHome.fxml
    ↓
Dashboard displayed
```

### 3. **Supplier Workflow**
```
Supplier Dashboard Loads
    ↓
Inventory Table populated from database
Orders Table populated from database
    ↓
Supplier can:
  • Add new food items (stored in food_items table)
  • View their inventory (filtered by supplier_username)
  • View orders from NGOs (from orders table)
  • Mark orders as completed
```

### 4. **NGO Workflow**
```
NGO Dashboard Loads
    ↓
Available Inventory displayed (all suppliers' items)
My Orders Table shows NGO's orders
    ↓
NGO can:
  • View all available food items from all suppliers
  • Place orders for items (creates entry in orders table)
  • View their order history
  • Track order status
```

---

## 🗄️ Database Design & SQL Connection

### Database Configuration
**File:** `DatabaseConfig.java`
```java
Database Path: resources/foodstorage.db
JDBC URL: jdbc:sqlite:resources/foodstorage.db
Database Type: SQLite (File-based, no server needed)
```

### Database Tables

#### **Table 1: food_items**
```sql
CREATE TABLE food_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT,
    quantity INTEGER DEFAULT 0,
    expiry_date TEXT,
    supplier_username TEXT REFERENCES users(username)
)
```
**Purpose:** Stores all food items in inventory
**Fields:**
- `id` - Unique identifier (auto-increment)
- `name` - Item name (Rice, Wheat, etc.)
- `category` - Category (Grains, Vegetables, etc.)
- `quantity` - Available quantity
- `expiry_date` - Expiration date (YYYY-MM-DD)
- `supplier_username` - Which supplier added this item

#### **Table 2: users**
```sql
CREATE TABLE users (
    username TEXT PRIMARY KEY,
    role TEXT CHECK(role IN ('NGO','SUPPLIER'))
)
```
**Purpose:** Store user accounts
**Fields:**
- `username` - Unique login identifier
- `role` - Either 'NGO' or 'SUPPLIER'

#### **Table 3: orders**
```sql
CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ngo_username TEXT NOT NULL REFERENCES users(username),
    item_id INTEGER NOT NULL,
    item_name TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    order_date TEXT NOT NULL,
    completed INTEGER DEFAULT 0
)
```
**Purpose:** Track food orders from NGOs
**Fields:**
- `id` - Order ID
- `ngo_username` - Which NGO placed the order
- `item_id` - Which food item
- `item_name` - Item name (for reference)
- `quantity` - Quantity ordered
- `order_date` - When order was placed
- `completed` - 0 = pending, 1 = completed

---

## 🔌 How Java Connects to Database (JDBC)

### SQL Connection Process

**Step 1: Load JDBC Driver**
```java
// In DBUtil.initializeDatabase()
Class.forName("org.sqlite.JDBC");
```
This loads the SQLite JDBC driver which understands SQLite database.

**Step 2: Get Database Connection**
```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DatabaseConfig.JDBC_URL);
}
```
- Uses `DriverManager` to create connection to SQLite
- JDBC URL points to the database file location
- Returns a `Connection` object for executing queries

**Step 3: Execute Queries**
```java
try (Connection connection = getConnection(); 
     Statement statement = connection.createStatement()) {
    statement.executeUpdate("INSERT INTO food_items ..."); // INSERT/UPDATE/DELETE
    ResultSet rs = statement.executeQuery("SELECT ..."); // SELECT queries
}
```

**Step 4: Close Connection**
- Try-with-resources automatically closes the connection
- Prevents database locks and memory leaks

### Key JDBC Classes Used

| Class | Purpose |
|-------|---------|
| `Connection` | Represents database connection |
| `Statement` | Executes raw SQL queries |
| `PreparedStatement` | Executes parameterized queries (safer against SQL injection) |
| `ResultSet` | Stores results from SELECT queries |
| `SQLException` | Handles database errors |

---

## 💻 Java Classes & Their Roles

### 1. **application/Main.java** - Entry Point
```java
public void start(Stage primaryStage) {
    // Initialize database tables
    DBUtil.initializeDatabase();
    
    // Load Login screen
    Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
    
    // Create and show window
    Scene scene = new Scene(root, 800, 500);
    primaryStage.setScene(scene);
    primaryStage.show();
}
```
**Role:** Starts the application, initializes database, shows GUI

### 2. **database/DatabaseConfig.java** - Configuration
```java
public static final String JDBC_URL = "jdbc:sqlite:resources/foodstorage.db";
public static final String TABLE_FOOD_ITEMS = "food_items";
public static final String COL_ID = "id";
// ... column names
```
**Role:** Centralized configuration for database connection and table/column names

### 3. **database/DBUtil.java** - Database Operations
```java
public static void initializeDatabase() { }          // Create tables
public static Connection getConnection() { }         // Get DB connection
public static ResultSet selectAllFoodItems() { }    // Fetch items
public static void insertFoodItem() { }             // Add item
public static void deleteFoodItemById() { }         // Delete item
public static void updateFoodItem() { }             // Modify item
```
**Role:** All database CRUD (Create, Read, Update, Delete) operations
**Key Methods:**
- `initializeDatabase()` - Creates tables if they don't exist
- `getConnection()` - Returns JDBC connection to database
- Various SELECT/INSERT/UPDATE/DELETE methods

### 4. **model/FoodItem.java** - Data Model
```java
public class FoodItem {
    private IntegerProperty id;
    private StringProperty name;
    private StringProperty category;
    private IntegerProperty quantity;
    private StringProperty expiryDate;
    private StringProperty supplierUsername;
    
    // Getters and properties for JavaFX binding
}
```
**Role:** Represents a food item
**Uses JavaFX Properties** for automatic UI updates (when data changes, table updates automatically)

### 5. **model/User.java** - User Model
```java
public class User {
    private String username;
    private String role; // "NGO" or "SUPPLIER"
}
```
**Role:** Represents a logged-in user

### 6. **model/Session.java** - Current Session
```java
public static void setCurrentUser(User user) { }
public static User getCurrentUser() { }
```
**Role:** Stores the currently logged-in user in memory (static singleton pattern)

### 7. **model/Order.java** - Order Model
```java
public class Order {
    private int id;
    private String ngoUsername;
    private String itemName;
    private int quantity;
    private String orderDate;
    private int completed;
}
```
**Role:** Represents an order placed by NGO

### 8. **controller/LoginController.java** - Login Logic
```java
@FXML private void handleLogin(ActionEvent e) {
    String username = usernameField.getText();
    String role = roleChoice.getValue();
    
    // Add user to database if new
    DBUtil.ensureUser(username, role);
    
    // Store in session
    Session.setCurrentUser(new User(username, role));
    
    // Load appropriate dashboard
    Parent root = FXMLLoader.load(role.equals("NGO") ? 
                                  "/NgoHome.fxml" : "/SupplierHome.fxml");
    stage.setScene(new Scene(root));
}
```
**Role:** Handles login process and role-based navigation

### 9. **controller/SupplierController.java** - Supplier Dashboard
```java
@FXML private void initialize() {
    // Set up table columns with data binding
    invColName.setCellValueFactory(new PropertyValueFactory<>("name"));
    
    // Load from database
    refreshInventory();
    refreshOrders();
}

private void refreshInventory() {
    // SQL Query to fetch supplier's items
    ResultSet rs = DBUtil.selectFoodItemsForSupplier(
        connection, 
        Session.getCurrentUser().getUsername()
    );
    // Convert ResultSet to ObservableList for table display
}
```
**Role:** Manages supplier dashboard
**Responsibilities:**
- Display supplier's food inventory
- Show orders from NGOs
- Add/edit/delete items
- Mark orders as completed

### 10. **controller/NgoController.java** - NGO Dashboard
```java
private void refreshInventory() {
    // SQL Query to fetch ALL items from all suppliers
    ResultSet rs = c.prepareStatement(
        "SELECT * FROM food_items ORDER BY name"
    ).executeQuery();
    // Display all available items
}

@FXML private void handlePlaceOrder() {
    // Insert into orders table
    // Decrease quantity in food_items table
}
```
**Role:** Manages NGO dashboard
**Responsibilities:**
- Display all available items from all suppliers
- Show items with supplier names
- Place orders for items
- View order history

---

## 🔄 Key Workflows Explained

### Workflow 1: Supplier Adds Food Item
```
Supplier enters:
  ├─ Name: "Rice"
  ├─ Category: "Grains"
  ├─ Quantity: 100
  └─ Expiry Date: "2025-12-31"
        ↓
handleAddItem() in SupplierController called
        ↓
DBUtil.insertFoodItemForSupplier(connection, 
                                 supplierUsername,
                                 "Rice", "Grains", 100, "2025-12-31")
        ↓
SQL EXECUTED:
INSERT INTO food_items (name, category, quantity, expiry_date, supplier_username)
VALUES ('Rice', 'Grains', 100, '2025-12-31', 'supplier1')
        ↓
Item appears in supplier's inventory table
Item becomes available for NGOs to order
```

### Workflow 2: NGO Places Order
```
NGO selects item and quantity
        ↓
handlePlaceOrder() in NgoController called
        ↓
SQL EXECUTED:
INSERT INTO orders (ngo_username, item_id, item_name, quantity, order_date, completed)
VALUES ('ngo1', 5, 'Rice', 50, '2025-11-15', 0)
        ↓
AND
UPDATE food_items SET quantity = quantity - 50 WHERE id = 5
        ↓
Order appears in NGO's "My Orders" table
Item quantity decreases in inventory
```

### Workflow 3: Supplier Marks Order Complete
```
Supplier sees pending order from NGO
        ↓
Clicks "Mark Complete" button
        ↓
SQL EXECUTED:
UPDATE orders SET completed = 1 WHERE id = 123
        ↓
Order disappears from pending list
Status updated in database
```

---

## 🎨 JavaFX Features Used

### 1. **TableView** - Display tabular data
```java
@FXML private TableView<FoodItem> tableView;
@FXML private TableColumn<FoodItem, String> colName;

// Bind column to property
colName.setCellValueFactory(new PropertyValueFactory<>("name"));
tableView.setItems(observableList);
```

### 2. **ObservableList** - Auto-updating lists
```java
ObservableList<FoodItem> items = FXCollections.observableArrayList();
// When items list changes, table updates automatically
items.add(newItem); // Table refreshes instantly
```

### 3. **FXML** - XML-based UI definition
```xml
<TableView fx:id="tableView">
    <columns>
        <TableColumn text="Name" fx:id="colName" />
    </columns>
</TableView>
```

### 4. **Data Binding** - Property system
```java
FoodItem has: StringProperty name = new SimpleStringProperty("Rice");
When name changes → UI updates automatically
```

### 5. **Color Coding** - Expiry alerts
```java
// In MainController
if (daysUntilExpiry <= 0) {
    row.setStyle("-fx-background-color: #ffcccb;"); // Red - Expired
} else if (daysUntilExpiry <= 7) {
    row.setStyle("-fx-background-color: #ffe4e1;"); // Pink - Expiring soon
} else if (quantity < 10) {
    row.setStyle("-fx-background-color: #ffeaa7;"); // Orange - Low stock
}
```

---

## 📦 Dependencies & Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| JavaFX | 25.0.1 | GUI framework |
| SQLite JDBC | 3.46.1.0 | Database driver |
| SLF4J API | 2.0.13 | Logging |
| SLF4J Simple | 2.0.13 | Logging implementation |

---

## 🚀 How to Run

### Compilation
```bash
javac --module-path "javafx-sdk-17.0.2/lib" \
      --add-modules javafx.controls,javafx.fxml \
      -d bin \
      -cp "src;resources;lib/*" \
      src/application/Main.java \
      src/controller/*.java \
      src/database/*.java \
      src/model/*.java
```

### Execution
```bash
java --module-path "javafx-sdk-17.0.2/lib" \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=ALL-UNNAMED \
     -cp "bin;resources;lib/*" \
     application.Main
```

---

## 🔐 Data Persistence

### Database Location
- **Path:** `resources/foodstorage.db`
- **Type:** SQLite (file-based database)
- **Created:** Automatically on first run
- **Persistent:** Data survives application restart

### Data Flow
```
User Action → Controller → DBUtil → JDBC → SQLite Database → foodstorage.db
                                 ↓
                          Data persisted
```

---

## 🐛 Error Handling

### Try-Catch Pattern
```java
try {
    Connection conn = DBUtil.getConnection();
    // Execute queries
    rs = statement.executeQuery(...);
} catch (SQLException e) {
    e.printStackTrace();
    // Show error to user
}
```

### Database Initialization Fallback
```java
try {
    statement.executeUpdate("ALTER TABLE orders ADD COLUMN completed ...");
} catch (SQLException ignore) {
    // Column already exists - ignore error
}
```

---

## 📊 Key Features

1. **Role-Based Access Control** - NGO vs Supplier different views
2. **Real-Time Inventory** - Automatic quantity updates
3. **Expiry Tracking** - Visual alerts for expiring items
4. **Order Management** - NGOs can place orders, suppliers can fulfill
5. **Data Persistence** - SQLite keeps data between sessions
6. **Search Functionality** - Find items by name or category
7. **Export to CSV** - Save inventory reports

---

## 🎯 Summary

**What:** Food inventory management system with two user roles
**How:** JavaFX GUI + SQLite database + JDBC connection
**Where:** Local file-based database (no server needed)
**Why:** Enables NGOs and Suppliers to manage food distribution efficiently
**Database:** 3 tables (food_items, users, orders) connected by foreign keys
**Flow:** User logs in → Role determines dashboard → CRUD operations on database → Persistent storage

This architecture ensures **data integrity, separation of concerns, and scalability**.

