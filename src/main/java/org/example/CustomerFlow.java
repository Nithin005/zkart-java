package org.example;

import org.example.protos.Invoice;
import org.example.protos.InvoiceItem;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerFlow {
    private final Customer customer;

    public List<Item> getCartItems() {
        return cartItems;
    }

    private List<Item> cartItems = new ArrayList<>();

    public List<Invoice> getInvoices() {
        return invoices;
    }

    private List<Invoice> invoices = new ArrayList<>();

    CustomerFlow(Customer customer, List<Invoice> invoices){
        this.customer = customer;
        this.invoices = invoices;
    }

    private Item getHighestStockItem(DBHelper dbHelper){
        List<Item> res = dbHelper.queryItems("WHERE stock = (SELECT MAX(stock) FROM inventory)");
        if(res.isEmpty()){
            return null;
        }
        return res.get(0);
    }

    public void shopFlow(DBHelper dbHelper){
        // shop
        // select category
        while(true) {
            List<String> categories = List.of("Mobile", "Laptop", "Tablet", "Go back");
            int categoryChoice = Utils.promptChoice(categories);
            if (categoryChoice == 3) {
                return;
            }
            List<Item> items;
            items = dbHelper.queryItems("WHERE category='" + categories.get(categoryChoice) + "'");
            Item newCartItem = Utils.promptItems(items);
            if (newCartItem == null) {
                return;
            }
            if (newCartItem.getStock() == 0) {
                System.out.println("no stock");
                return;
            }
            for (Item cartItem : cartItems) {
                if (cartItem.getItemId() == newCartItem.getItemId()) {
                    // already in cart
                    if (cartItem.getQuantity() + 1 > cartItem.getStock()) {
                        System.out.println("cannot add any more items");
                        return;
                    }
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    System.out.println("item added to cart");
                    return;
                }
            }

            // new item
            cartItems.add(newCartItem);
            System.out.println("item added to cart");
        }
    }

    public Invoice checkout(DBHelper dbHelper){
        // check discount eligibility
        if(cartItems.isEmpty()) {
            System.out.println("cart is empty");
            return null;
        }
        int numPurchases = invoices.size();
        Item dealOfTheMoment = getHighestStockItem(dbHelper);
        int totalCartValue = Utils.getCartValue(cartItems, dealOfTheMoment);
        String userDiscountCode = dbHelper.getDiscountCode(customer);
        if(numPurchases == 4 | totalCartValue >= 20_000) {
            userDiscountCode = Utils.generateDiscountCode();
            dbHelper.updateDiscountCode(customer, userDiscountCode);
        }
        int discountedCartValue = totalCartValue;
        if(userDiscountCode != null){
            boolean useDiscount = Utils.promptBoolean("found discount code ("+userDiscountCode+"). do you want to use it? (y/N): ");
            if(useDiscount){
                int discountPercent = Utils.generateDiscountValue();
                discountedCartValue = totalCartValue * (100 - discountPercent) / 100;
                System.out.printf("applied discount | %d -> %d (%d%% discount)\n", totalCartValue, discountedCartValue, discountPercent);
                dbHelper.invalidateDiscountCode(customer);
            }
        }
        String invoiceNo = Utils.generateInvoiceNumber();
        Invoice.Builder invoiceBuilder = Invoice.newBuilder().setInvoiceNo(invoiceNo)
                .setDatetime(LocalDateTime.now().toString())
                .setEmail(customer.getEmail())
                .setTotalValue(totalCartValue)
                .setDiscountedValue(discountedCartValue);
        for (Item item : cartItems) {
            InvoiceItem invoiceItem = item.toInvoiceItem();
            invoiceBuilder.addItems(invoiceItem);
        }
        Invoice invoice = invoiceBuilder.build();

        // update stocks in db
        for(Item item: cartItems){
            dbHelper.updateStock(item, item.getStock()-item.getQuantity());
        }
        if(userDiscountCode != null){
            dbHelper.updateDiscountCodeExpiry(customer);
        }
        cartItems.clear();
        invoices.add(invoice);
        return invoice;
    }

 }
