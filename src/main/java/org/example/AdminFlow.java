package org.example;

import java.util.List;
import java.util.Objects;

public class AdminFlow {

    private Customer customer;
    public static String defaultEncyptedPwd = "yzaaz";

    AdminFlow(Customer customer){
        this.customer = customer;
    }

    public void restockItems(DBHelper dbHelper){
        int threshold = Utils.promptInt("Enter stock threshold: ");
        List<Item> items = dbHelper.queryItems("WHERE stock <= "+ threshold + ";");
        Item item = Utils.promptItems(items);
        if(item == null) {
            return;
        }
        int newStock = Utils.promptInt("Enter stock to be added: ");
        dbHelper.updateStock(item, newStock);
        System.out.println("stock updated");
    }

    public boolean isAdminDefaultPwd(){
        System.out.println(this.customer.getEncryptedPwd());
        return defaultEncyptedPwd.equals(this.customer.getEncryptedPwd());
    }

    public static boolean isAdminUsername(String username){
        return "admin@zoho.com".equals(username);
    }

}
