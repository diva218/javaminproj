# Food Storage Management System - Setup Guide

## 🎯 Project Overview
A comprehensive JavaFX application for managing food inventory with MySQL database integration, designed to support **SDG 2: Zero Hunger** through efficient food storage management and waste reduction.

## 📋 Prerequisites

### 1. Java Development Kit (JDK)
- **Required**: JDK 17 or higher
- **Current**: OpenJDK 17.0.16 (verified working)

### 2. MySQL Database Server
- **Required**: MySQL 8.0 or higher
- **Installation**: Download from [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)
- **Default Configuration**:
  - Host: `localhost`
  - Port: `3306`
  - Username: `root`
  - Password: `` (empty - change in `DatabaseConfig.java` if different)

## 🚀 Quick Start

### Option 1: One-Click Setup
```batch
# Double-click this file to build and run
build-and-run.bat
```

### Option 2: Manual Steps
```batch
# 1. Compile the project
compile.bat

# 2. Run the application
run.bat
```

### Option 3: Command Line
```batch
# Compile
javac -cp "lib\*;javafx-sdk-17.0.2\lib\*" -d out src\application\*.java src\controller\*.java src\database\*.java src\model\*.java

# Copy resources
copy resources\MainView.fxml out\

# Run
java --module-path javafx-sdk-17.0.2\lib --add-modules javafx.controls,javafx.fxml -cp "out;lib\*" application.Main
```

## 📁 Project Structure
```
javaproject/
├── src/
│   ├── application/
│   │   └── Main.java                 # JavaFX Application entry point
│   ├── controller/
│   │   └── MainController.java       # UI controller with all features
│   ├── database/
│   │   ├── DBUtil.java              # Database operations
│   │   └── DatabaseConfig.java      # Database configuration
│   └── model/
│       └── FoodItem.java            # Data model
├── resources/
│   └── MainView.fxml                # UI layout (modern design)
├── lib/                             # Dependencies
├── javafx-sdk-17.0.2/              # JavaFX runtime
└── out/                             # Compiled classes
```

## 🔧 Database Configuration

### Automatic Setup
The application will automatically:
1. Create database `food_storage_db` if it doesn't exist
2. Create table `food_items` with proper schema
3. Handle all database connections

### Manual Configuration (if needed)
Edit `src/database/DatabaseConfig.java`:
```java
public static final String DB_HOST = "localhost";
public static final String DB_PORT = "3306";
public static final String DB_NAME = "food_storage_db";
public static final String DB_USERNAME = "root";
public static final String DB_PASSWORD = "your_password_here";
```

## ✨ Features Implemented

### ✅ Basic Functionality
1. **MySQL Connectivity** - Full database integration
2. **Load Data** - Fetch and display all food items
3. **Insert Data** - Add new food items with validation
4. **Delete Data** - Remove items with confirmation
5. **Update Item** - Edit existing items inline

### ✅ Advanced Features
6. **Search & Filter** - Real-time search by name/category
7. **Expiry Alerts** - Red highlighting for items expiring ≤7 days
8. **Low Stock Alert** - Orange highlighting for quantity <10
9. **Export Feature** - Export inventory to CSV with status

### ✅ UI/UX Improvements
12. **Modern Design** - Clean, professional interface
13. **Color-coded Alerts** - Visual status indicators
14. **Responsive Layout** - Organized sections with proper spacing

### ✅ SDG Integration
11. **SDG 2 Support** - Header text emphasizes hunger reduction mission

## 🎨 User Interface Guide

### Color Legend
- 🔴 **Red Background**: Expired or expiring within 7 days
- 🟠 **Orange Background**: Low stock (quantity < 10)
- ✅ **Normal**: Adequate stock and not expiring soon

### Main Sections
1. **Header**: Title, mission statement, search bar, export button
2. **Table**: Color-coded inventory display with sorting
3. **Form**: Add/edit items with validation and clear labels
4. **Actions**: Add, Edit, Delete buttons with icons

## 📊 Export Functionality
- **Format**: CSV (Comma Separated Values)
- **Filename**: `food_inventory_YYYY-MM-DD.csv`
- **Columns**: ID, Name, Category, Quantity, Expiry Date, Status
- **Status Values**: Normal, Expired, Expiring Soon, Low Stock

## 🔍 Search & Filter
- **Real-time**: Results update as you type
- **Fields**: Searches both name and category
- **Case-insensitive**: Works with any capitalization
- **Clear button**: Reset search instantly

## ⚠️ Troubleshooting

### MySQL Connection Issues
```
Error: "Failed to create database"
Solution: 
1. Ensure MySQL server is running
2. Check username/password in DatabaseConfig.java
3. Verify MySQL is accessible on localhost:3306
```

### JavaFX Runtime Issues
```
Error: "JavaFX runtime components are missing"
Solution: The project includes JavaFX SDK - ensure javafx-sdk-17.0.2 folder exists
```

### Compilation Errors
```
Error: "package does not exist"
Solution: 
1. Ensure all JAR files are in lib/ directory
2. Check classpath in build commands
3. Verify JDK version is 17+
```

## 📈 Future Enhancements (Roadmap)

### Phase 2 Features
- **Distribution Records** - Track food distribution with recipient details
- **Reports Generation** - Summary reports with charts
- **Login System** - Multi-user access with authentication
- **Splash Screen** - Welcome screen with mission statement

### Phase 3 Features
- **Data Analytics** - Trend analysis and predictions
- **Barcode Scanning** - Quick item entry
- **Mobile App** - Companion mobile application
- **Cloud Sync** - Multi-location inventory management

## 🤝 Contributing to SDG 2: Zero Hunger

This application directly supports **UN Sustainable Development Goal 2** by:

1. **Reducing Food Waste**: Expiry alerts prevent food spoilage
2. **Efficient Inventory**: Better tracking reduces over/under-stocking
3. **Distribution Tracking**: Monitor food distribution to those in need
4. **Data-Driven Decisions**: Export and analyze inventory patterns
5. **Accessibility**: Simple interface for NGO staff and volunteers

## 📞 Support

For technical support or feature requests:
1. Check this setup guide first
2. Review error messages carefully
3. Ensure all prerequisites are met
4. Verify database connectivity

---
**Version**: 2.0  
**Last Updated**: October 2024  
**License**: Open Source for humanitarian use
