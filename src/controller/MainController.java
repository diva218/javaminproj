package controller;

import database.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FoodItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class MainController {
    @FXML private TableView<FoodItem> tableView;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, String> colCategory;
    @FXML private TableColumn<FoodItem, Integer> colQuantity;
    @FXML private TableColumn<FoodItem, String> colExpiry;

    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField quantityField;
    @FXML private TextField expiryDateField;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button editButton;
    @FXML private Button exportButton;
    @FXML private Button clearSearchButton;

    private final ObservableList<FoodItem> items = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> filteredItems = FXCollections.observableArrayList();
    private FoodItem selectedItemForEdit = null;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupTableRowFactory();
        tableView.setItems(filteredItems);
        setupSearchFunctionality();
        refreshTable();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
    }
    
    private void setupTableRowFactory() {
        tableView.setRowFactory(tv -> {
            TableRow<FoodItem> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    // Check for expiry alerts (within 7 days)
                    LocalDate today = LocalDate.now();
                    LocalDate expiryDate = parseDate(newItem.getExpiryDate());
                    
                    if (expiryDate != null) {
                        long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate);
                        
                        if (daysUntilExpiry <= 0) {
                            // Expired items - dark red
                            row.setStyle("-fx-background-color: #ffcccb; -fx-text-fill: #8b0000;");
                        } else if (daysUntilExpiry <= 7) {
                            // Expiring soon - light red
                            row.setStyle("-fx-background-color: #ffe4e1; -fx-text-fill: #dc143c;");
                        } else if (newItem.getQuantity() < 10) {
                            // Low stock - orange
                            row.setStyle("-fx-background-color: #ffeaa7; -fx-text-fill: #e17055;");
                        } else {
                            row.setStyle("");
                        }
                    } else if (newItem.getQuantity() < 10) {
                        // Low stock - orange
                        row.setStyle("-fx-background-color: #ffeaa7; -fx-text-fill: #e17055;");
                    } else {
                        row.setStyle("");
                    }
                }
            });
            return row;
        });
    }
    
    private void setupSearchFunctionality() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterItems(newValue);
            });
        }
    }
    
    private void filterItems(String searchTerm) {
        filteredItems.clear();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredItems.addAll(items);
        } else {
            String lowerCaseFilter = searchTerm.toLowerCase();
            for (FoodItem item : items) {
                if (item.getName().toLowerCase().contains(lowerCaseFilter) ||
                    item.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                    filteredItems.add(item);
                }
            }
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String category = categoryField.getText() == null ? "" : categoryField.getText().trim();
        String quantityText = quantityField.getText() == null ? "0" : quantityField.getText().trim();
        String expiry = expiryDateField.getText() == null ? "" : expiryDateField.getText().trim();

        // Validation
        if (name.isEmpty()) {
            showAlert("Validation Error", "Name cannot be empty!");
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity < 0) {
                showAlert("Validation Error", "Quantity cannot be negative!");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid quantity!");
            return;
        }
        
        // Validate date format
        if (!expiry.isEmpty()) {
            try {
                LocalDate.parse(expiry);
            } catch (Exception e) {
                showAlert("Validation Error", "Please enter date in YYYY-MM-DD format!");
                return;
            }
        }

        try (Connection connection = DBUtil.getConnection()) {
            if (selectedItemForEdit != null) {
                // Update existing item
                DBUtil.updateFoodItem(connection, selectedItemForEdit.getId(), name, category, quantity, expiry);
                selectedItemForEdit = null;
                addButton.setText("Add");
            } else {
                // Insert new item
                DBUtil.insertFoodItem(connection, name, category, quantity, expiry);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save item: " + e.getMessage());
            return;
        }

        clearInputFields();
        refreshTable();
    }

    @FXML
    private void handleDelete() {
        FoodItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select an item to delete!");
            return;
        }
        
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Food Item");
        confirmAlert.setContentText("Are you sure you want to delete '" + selected.getName() + "'?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection connection = DBUtil.getConnection()) {
                DBUtil.deleteFoodItemById(connection, selected.getId());
                refreshTable();
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete item: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleEdit() {
        FoodItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select an item to edit!");
            return;
        }
        
        // Populate fields with selected item data
        nameField.setText(selected.getName());
        categoryField.setText(selected.getCategory());
        quantityField.setText(String.valueOf(selected.getQuantity()));
        expiryDateField.setText(selected.getExpiryDate());
        
        selectedItemForEdit = selected;
        addButton.setText("Update");
    }
    
    @FXML
    private void handleClearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Inventory Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("food_inventory_" + LocalDate.now() + ".csv");
        
        Stage stage = (Stage) tableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            exportToCSV(file);
        }
    }
    
    private void exportToCSV(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append("ID,Name,Category,Quantity,Expiry Date,Status\n");
            
            // Write data
            LocalDate today = LocalDate.now();
            for (FoodItem item : items) {
                writer.append(String.valueOf(item.getId())).append(",");
                writer.append(item.getName()).append(",");
                writer.append(item.getCategory()).append(",");
                writer.append(String.valueOf(item.getQuantity())).append(",");
                writer.append(item.getExpiryDate()).append(",");
                
                // Add status based on expiry and quantity
                LocalDate expiryDate = parseDate(item.getExpiryDate());
                String status = "Normal";
                if (expiryDate != null) {
                    long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate);
                    if (daysUntilExpiry <= 0) {
                        status = "Expired";
                    } else if (daysUntilExpiry <= 7) {
                        status = "Expiring Soon";
                    }
                }
                if (item.getQuantity() < 10) {
                    status += (status.equals("Normal") ? "Low Stock" : ", Low Stock");
                }
                
                writer.append(status).append("\n");
            }
            
            showAlert("Export Successful", "Data exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Export Error", "Failed to export data: " + e.getMessage());
        }
    }

    private void refreshTable() {
        items.clear();
        try (Connection connection = DBUtil.getConnection(); 
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, name, category, quantity, expiry_date FROM food_items ORDER BY name");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                int quantity = rs.getInt("quantity");
                String expiry = rs.getString("expiry_date");
                items.add(new FoodItem(id, name, category, quantity, expiry));
            }
            // Update filtered items
            String currentSearch = searchField != null ? searchField.getText() : "";
            filterItems(currentSearch);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load items: " + e.getMessage());
        }
    }

    private void clearInputFields() {
        nameField.clear();
        categoryField.clear();
        quantityField.clear();
        expiryDateField.clear();
        selectedItemForEdit = null;
        addButton.setText("Add");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


