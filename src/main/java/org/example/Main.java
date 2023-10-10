package org.example;

import org.example.protos.Invoice;
import org.example.protos.Invoices;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        // config
        String CUSTOMER_DB_TEXT = "zusers_db.txt";
        String INVENTORY_DB_TEXT = "z-kart_db.txt";
        String INVOICE_PROTO = "invoice.txtpb";
        String PWD_HISTORY_FILE = "pwdHistory.dat";

        DBHelper dbHelper;
        Customer customer = null;

        boolean loadFromFile = true;

//        if (!Files.exists(Path.of("sample.db"))){
//            loadFromFile = true;
//            System.out.println("running program for the first time. recommend to press (Y) for next prompt");
//        }
        try {
//            dbHelper = new DBHelper("jdbc:sqlite:sample.db");
            dbHelper = new DBHelper("jdbc:mysql://localhost:3306/test?" +
                    "user=test&password=test");
        } catch (SQLException e){
            throw  new RuntimeException(e);
        }
        Path invoiceProtoPath = Path.of(INVOICE_PROTO);
        if (loadFromFile){
            if(Utils.promptBoolean("loadFromFile is ENABLED, DB will init from text files\nall previous data will be deleted. do you want to continue (y/N): ")){
                Utils.cleanFiles();
                Utils.cleanFile(invoiceProtoPath);
                dbHelper.readFromTextFile(Path.of(CUSTOMER_DB_TEXT), Path.of(INVENTORY_DB_TEXT));
            } else {
                System.out.println("continuing safely");
            }
        }

        outer:
        while (true) {
            // login flow
            while(customer == null){
                customer = loginFlow(dbHelper, PWD_HISTORY_FILE);
                if (customer == null) {
                    System.out.println("loginFlow invalid");
                }
            }

            if(customer.isAdmin()){
                AdminFlow adminFlow = new AdminFlow(customer);

                inner_admin:
                while(true){
                    int choice;
                    if(adminFlow.isAdminDefaultPwd()){
                        System.out.println("change default password");
                        choice = 1;
                    } else {
                        choice = Utils.promptChoice(List.of("List low stock items", "Change password", "Exit"));
                    }
                    switch (choice){
                        case 0:
                            // list low stock items
                            adminFlow.restockItems(dbHelper);
                            break;
                        case 1:
                            // change password
                            boolean success = false;
                            String pwd;
                            do {
                                pwd = Utils.promptChangePassword();
                                success = changePassword(pwd, customer.getCustomerId(), PWD_HISTORY_FILE);
                                if(!success){
                                    System.out.println("password change failed");
                                }
                            } while (!success);
                            Utils.updateAdminPwd(pwd);
                            customer.setPassword(pwd);
                            System.out.println("password changed");
                            break;
                        case 2:
                            break inner_admin;
                    }
                }
            } else {
                // load invoices
                List<Invoice> userInvoices = Utils.readInvoices(customer, invoiceProtoPath);
                CustomerFlow customerFlow = new CustomerFlow(customer, userInvoices);
                inner_customer:
                while (true) {
                    // deal of the moment
                    System.out.println("/".repeat(10)+" DEAL OF THE MOMENT (10% discount) "+"/".repeat(10));
                    Utils.printItems(List.of(customerFlow.getDealOfTheMomentItem(dbHelper)));
                    int choice = Utils.promptChoice(List.of("Shop", "View Cart", "Checkout", "View invoices", "Change password", "view invoices range", "Exit"));
                    switch (choice){
                        case 0:
                            // shop
                            customerFlow.shopFlow(dbHelper);
                            break;
                        case 1:
                            // view cart
                            List<Item> cartItems = customerFlow.getCartItems();
                            if(cartItems.isEmpty()){
                                System.out.println("no items in cart");
                            } else {
                                Utils.printCartItems(cartItems);
                            }

                            break;
                        case 2:
                            // checkout
                            Invoice invoice = customerFlow.checkout(dbHelper);
                            if (invoice == null){
                                continue;
                            }
                            Utils.saveInvoice(invoice, invoiceProtoPath);
                            break;
                        case 3:
                            // view invoices
                            List<Invoice> _userInvoices = customerFlow.getInvoices();
                            if(_userInvoices.isEmpty()){
                                System.out.println("no invoices");
                            } else {
                                Utils.printInvoices(_userInvoices);
                            }
                            break;
                        case 4:
                            // change password
                            boolean success = false;
                            String pwd;
                            do {
                                pwd = Utils.promptChangePassword();
                                success = changePassword(pwd, customer.getCustomerId(), PWD_HISTORY_FILE);
                                if(!success){
                                    System.out.println("password change failed");
                                }
                            } while(!success);
                            dbHelper.updateCustomerPwd(customer, pwd);
                            customer.setPassword(pwd);
                            System.out.println("password changed");
                            break;
                        case 5:
                            // view invoices range
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            String fromDateString = Utils.promptStringLine("Enter from date (yyyy-MM-dd HH:mm:ss): ");
                            LocalDateTime fromDate = LocalDateTime.parse(fromDateString, formatter);
                            String toDateString = Utils.promptStringLine("Enter to date (yyyy-MM-dd HH:mm:ss): ");
                            LocalDateTime toDate = LocalDateTime.parse(toDateString, formatter);
                            List<Invoice> invoicesInRange = new ArrayList<>();

                            for(Invoice inv: customerFlow.getInvoices()){
                                LocalDateTime invoiceDateTime = LocalDateTime.parse(inv.getDatetime());
                                if(invoiceDateTime.isAfter(fromDate) && invoiceDateTime.isBefore(toDate)){
                                    invoicesInRange.add(inv);
                                }
                            }
                            if(invoicesInRange.isEmpty()){
                                System.out.println("no invoices found in range");
                            } else {
                                Utils.printInvoices(invoicesInRange);
                            }
                            break;
                        case 6:
                            // exit
                            break inner_customer;
                    }

                }
            }
            // clean up
            customer = null;
        }
    }

    public static Customer loginFlow(DBHelper dbHelper, String pwdHistoryFile){
        int choice = Utils.promptChoice(List.of("Login", "Register", "Exit"));
        String emailId;
        String pwd;
        switch(choice){
            case 0:
                // login
                emailId = Utils.promptString("Enter username/emailId: ");
                pwd = Utils.promptString("Enter password: ");

                if(AdminFlow.isAdminUsername(emailId)){
                    if(Utils.getAdminEncryptedPwd().equals(Utils.encryptPwd(pwd))){
                        Customer adminUser = new Customer(-99, emailId, Utils.encryptPwd(pwd), "admin", "9999999999");
                        adminUser.setAdmin();
                        return adminUser;
                    } else {
                        System.out.println("user dosent exists");
                        return null;
                    }
                }
                // normal customer
                Customer curCustomer = dbHelper.getCustomer(emailId);
                if (curCustomer == null) {
                    System.out.println("user dosent exists");
                    return null;
                }
                boolean valid = curCustomer.validatePassword(pwd);
                if(!valid){
                    System.out.println("username/password is wrong");
                    return null;
                }
                System.out.println("user is authenticated");
                return curCustomer;
            case 1:
                // register
                String newEmailId = Utils.promptEmail("Enter username/emailId: ");
                String newName = Utils.promptString("Enter name: ");
                String newMobile = Utils.promptMobileNo("Enter mobileNo: ");
                Customer newCustomer = new Customer(0, newEmailId, "", newName, newMobile);
                boolean success = false;
//                String pwd;
                do {
                    pwd = Utils.promptChangePassword();
                    success = changePassword(pwd, newCustomer.getCustomerId(), pwdHistoryFile);
                    if(!success){
                        System.out.println("password change failed");
                    }
                } while(!success);
                newCustomer.setPassword(pwd);
                int customerId = dbHelper.addCustomer(newCustomer);
                newCustomer.setCustomerId(customerId);
                System.out.println("new account created");
                return newCustomer;
            case 2:
                System.exit(0);
        }
        return null;
    }

    public static boolean changePassword(String pwd, Integer customer_id, String pwdHistoryFile){
        // 1. check prev 3 passwords
        Path pwdHistoryFilePath = Path.of(pwdHistoryFile);
        Map<Integer, List<String>> pwdHistory = Utils.readPwdHistory(pwdHistoryFilePath);
        List<String> userPwdHistory = pwdHistory.getOrDefault(customer_id, new ArrayList<>(List.of("", "", "")));
        if(userPwdHistory.contains(pwd)){
            System.out.println("password cannot be same as previous three passwords");
            return false;
        }
        Collections.rotate(userPwdHistory, 1);
        userPwdHistory.set(0, pwd);
        pwdHistory.put(customer_id, userPwdHistory);
        Utils.savePwdHistory(pwdHistoryFilePath, pwdHistory);
        return true;
    }
}