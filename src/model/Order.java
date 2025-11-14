package model;

public class Order {
    private final int id;
    private final String ngoUsername;
    private final int itemId;
    private final String itemName;
    private final int quantity;
    private final String orderDate; // ISO-8601 string

    public Order(int id, String ngoUsername, int itemId, String itemName, int quantity, String orderDate) {
        this.id = id;
        this.ngoUsername = ngoUsername;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.orderDate = orderDate;
    }

    public int getId() { return id; }
    public String getNgoUsername() { return ngoUsername; }
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public String getOrderDate() { return orderDate; }
}
