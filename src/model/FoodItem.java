package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FoodItem {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final IntegerProperty quantity;
    private final StringProperty expiryDate;

    public FoodItem(int id, String name, String category, int quantity, String expiryDate) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.expiryDate = new SimpleStringProperty(expiryDate);
    }

    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }

    public StringProperty categoryProperty() { return category; }
    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }

    public IntegerProperty quantityProperty() { return quantity; }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(value); }

    public StringProperty expiryDateProperty() { return expiryDate; }
    public String getExpiryDate() { return expiryDate.get(); }
    public void setExpiryDate(String value) { expiryDate.set(value); }
}


