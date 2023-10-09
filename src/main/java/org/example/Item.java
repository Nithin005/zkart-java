package org.example;

import org.example.protos.InvoiceItem;

public class Item {
    private final int itemId;
    private final String category;
    private final String brand;
    private final String model;
    private int price;
    private int stock;
    private int quantity = 1;

    public Item(int itemId, String category, String brand, String model, int price, int stock) {
        this.itemId = itemId;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.stock = stock;
    }

    public int getItemId() {
        return itemId;

    }
    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public InvoiceItem toInvoiceItem(){
        return InvoiceItem.newBuilder().setCategory(this.category)
                .setItemId(this.itemId)
                .setBrand(this.brand)
                .setModel(this.model)
                .setPrice(this.price)
                .setQuantity(this.quantity)
                .build();
    }
}
