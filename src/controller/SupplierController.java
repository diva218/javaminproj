package controller;

import database.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.FoodItem;
import model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SupplierController {
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField qtyField;
    @FXML private TextField expiryField;
    @FXML private Label statusLabel;

    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, Integer> invColId;
    @FXML private TableColumn<FoodItem, String> invColName;
    @FXML private TableColumn<FoodItem, String> invColCategory;
    @FXML private TableColumn<FoodItem, Integer> invColQty;
    @FXML private TableColumn<FoodItem, String> invColExpiry;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> ordColId;
    @FXML private TableColumn<Order, String> ordColNgo;
    @FXML private TableColumn<Order, String> ordColItem;
    @FXML private TableColumn<Order, Integer> ordColQty;
    @FXML private TableColumn<Order, String> ordColDate;

    private final ObservableList<FoodItem> inventory = FXCollections.observableArrayList();
    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        invColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        invColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        invColCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        invColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invColExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        inventoryTable.setItems(inventory);

        ordColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        ordColNgo.setCellValueFactory(new PropertyValueFactory<>("ngoUsername"));
        ordColItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        ordColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        ordColDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        ordersTable.setItems(allOrders);

        refreshInventory();
        refreshOrders();
    }

    private void refreshInventory() {
        inventory.clear();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, category, quantity, expiry_date FROM food_items ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                inventory.add(new FoodItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getString("expiry_date")
                ));
            }
        } catch (SQLException e) {
            statusLabel.setText("DB error: " + e.getMessage());
        }
    }

    private void refreshOrders() {
        allOrders.clear();
        try (Connection c = DBUtil.getConnection();
             ResultSet rs = DBUtil.selectAllOrders(c)) {
            while (rs.next()) {
                allOrders.add(new Order(
                        rs.getInt("id"),
                        rs.getString("ngo_username"),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getString("order_date")
                ));
            }
        } catch (SQLException e) {
            statusLabel.setText("DB error: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddStock() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String category = categoryField.getText() == null ? "" : categoryField.getText().trim();
        int qty;
        try { qty = Integer.parseInt(qtyField.getText()); } catch (Exception ex) { statusLabel.setText("Invalid qty"); return; }
        String expiry = expiryField.getText() == null ? "" : expiryField.getText().trim();
        if (name.isEmpty()) { statusLabel.setText("Name required"); return; }
        if (qty <= 0) { statusLabel.setText("Qty must be > 0"); return; }
        try (Connection c = DBUtil.getConnection()) {
            DBUtil.insertFoodItem(c, name, category, qty, expiry);
            statusLabel.setText("Stock added");
            nameField.clear(); categoryField.clear(); qtyField.clear(); expiryField.clear();
            refreshInventory();
        } catch (SQLException e) {
            statusLabel.setText("Add failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSelected() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an item to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Food Item");
        confirm.setContentText("Are you sure you want to delete '" + selected.getName() + "'?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection c = DBUtil.getConnection()) {
                DBUtil.deleteFoodItemById(c, selected.getId());
                statusLabel.setText("Item deleted");
                refreshInventory();
            } catch (SQLException e) {
                statusLabel.setText("Delete failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleMarkOrderCompleted() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an order to mark completed");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Complete Order");
        confirm.setHeaderText("Mark Order as Completed");
        confirm.setContentText("Mark order #" + selected.getId() + " as completed?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DBUtil.markOrderCompleted(selected.getId());
                statusLabel.setText("Order marked completed");
                refreshOrders();
            } catch (SQLException e) {
                statusLabel.setText("Update failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            model.Session.setCurrentUser(null);
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle("Food Inventory - Login");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load login: " + ex.getMessage(), ButtonType.OK);
            alert.setHeaderText("Navigation Error");
            alert.showAndWait();
        }
    }
}
