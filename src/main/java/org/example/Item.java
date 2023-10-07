package org.example;

import org.example.protos.InvoiceItem;

class Item {
    private int itemId;
    private String category;
    private String brand;
    private String model;
    private int price;
    private int stock;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    private int quantity = 1;

    public Item(int itemId, String category, String brand, String model, int price, int stock) {
        this.itemId = itemId;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.stock = stock;
    }

    public void addStock(int num) {
        this.stock += num;
    }

    public void removeStock(int num) {
        this.stock -= num;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getItemId() { return itemId; }
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

    public String getItemKey() {
        return this.brand + "-" + this.model;
    }

    public InvoiceItem toInvoiceItem(){
        return InvoiceItem.newBuilder().setCategory(this.category)
                .setBrand(this.brand)
                .setModel(this.model)
                .setPrice(this.price)
                .setQuantity(this.quantity)
                .build();
    }
}
