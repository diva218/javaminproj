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
import model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class NgoController {
    @FXML private ComboBox<String> itemChoice;
    @FXML private TextField qtyField;
    @FXML private Label statusLabel;

    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, String> invColName;
    @FXML private TableColumn<FoodItem, String> invColCategory;
    @FXML private TableColumn<FoodItem, Integer> invColQty;
    @FXML private TableColumn<FoodItem, String> invColExpiry;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> ordColId;
    @FXML private TableColumn<Order, String> ordColItem;
    @FXML private TableColumn<Order, Integer> ordColQty;
    @FXML private TableColumn<Order, String> ordColDate;

    private final ObservableList<FoodItem> inventory = FXCollections.observableArrayList();
    private final ObservableList<Order> myOrders = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        invColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        invColCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        invColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invColExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        inventoryTable.setItems(inventory);

        ordColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        ordColItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        ordColQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        ordColDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        ordersTable.setItems(myOrders);

        refreshInventory();
        refreshOrders();
        refreshChoiceBox();
    }

    private void refreshInventory() {
        inventory.clear();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, category, quantity, expiry_date FROM food_items ORDER BY name");
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
        myOrders.clear();
        try (Connection c = DBUtil.getConnection();
             ResultSet rs = DBUtil.selectOrdersForNgo(c, Session.getCurrentUser().getUsername())) {
            while (rs.next()) {
                myOrders.add(new Order(
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

    private void refreshChoiceBox() {
        itemChoice.getItems().clear();
        for (FoodItem fi : inventory) {
            itemChoice.getItems().add(fi.getId() + ": " + fi.getName());
        }
        if (!itemChoice.getItems().isEmpty()) itemChoice.getSelectionModel().selectFirst();
    }

    @FXML
    private void handlePlaceOrder() {
        String selected = itemChoice.getValue();
        if (selected == null) { statusLabel.setText("Select an item"); return; }
        int idx = selected.indexOf(":");
        int itemId = Integer.parseInt(selected.substring(0, idx));
        String itemName = selected.substring(idx + 2);
        int qty;
        try { qty = Integer.parseInt(qtyField.getText()); } catch (Exception ex) { statusLabel.setText("Invalid qty"); return; }
        if (qty <= 0) { statusLabel.setText("Qty must be > 0"); return; }
        try {
            DBUtil.insertOrderAndDecrementStock(Session.getCurrentUser().getUsername(), itemId, itemName, qty, LocalDate.now().toString());
            statusLabel.setText("Order placed");
            refreshInventory();
            refreshChoiceBox();
            refreshOrders();
        } catch (SQLException e) {
            statusLabel.setText("Order failed: " + e.getMessage());
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
