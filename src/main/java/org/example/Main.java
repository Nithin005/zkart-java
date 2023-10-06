package org.example;

import jdk.jshell.execution.Util;
import org.example.protos.Invoice;
import org.example.protos.Invoices;
import org.example.protos.Item;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    private static String INVOICE_PROTO = "invoice.txtpb";
    private static DBHelper dbHelper;
    private static Modal.Customer customer;
    private static List<Modal.Item> cart = new ArrayList<>();
    private static Map<String, Integer> numInCart = new HashMap<>();
    private static List<Invoice> userInvoices = new ArrayList<>();
    private static boolean isAdmin = false;
    private static String userDiscountCode;
    private static Modal.Item dealOfTheMoment;
    private static boolean dealOfTheMomentChanged = true;
    private static Map<String, List<String>> pwdHistory = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        String CUSTOMER_DB_TEXT = "zusers_db.txt";
        String INVENTORY_DB_TEXT = "z-kart_db.txt";
        boolean loadFromFile = false;

        if (!Files.exists(Path.of("sample.db"))){
            loadFromFile = true;
            System.out.println("running program for the first time. recommend to press (Y) for next prompt");
        }
        dbHelper = new DBHelper("jdbc:sqlite:sample.db");
        if (loadFromFile){
            if(Utils.promptBoolean("loadFromFile is ENABLED, DB will init from text files\nall previous data will be deleted. do you want to continue (y/N): ")){
                Utils.cleanFiles();
                Utils.cleanFile(Path.of(INVOICE_PROTO));
                dbHelper.readFromTextFile(Path.of(CUSTOMER_DB_TEXT), Path.of(INVENTORY_DB_TEXT));
            } else {
                System.out.println("continuing safely");
            }
        }

        // load pwd history
        Map<String, List<String>> _pwdHistory = Utils.readPwdHistory();
        if(_pwdHistory != null){
            pwdHistory = _pwdHistory;
        }


        // login flow
        customer = loginFlow(dbHelper);
        if(isAdmin){
            customer = Utils.getAdminCustomer();
            while(true){
                adminFlow(dbHelper);
            }
        } else {
            if (customer == null) {
                System.out.println("loginFlow invalid");
                System.exit(0);
            }
            dealOfTheMoment = Utils.getMaxStockItem(dbHelper.queryItems(""));
            while (true) {
                if(dealOfTheMomentChanged){
                    System.out.println("DEAL OF THE MOMENT (10% discount will be added during checkout): ");
                    Utils.printItems(List.of(dealOfTheMoment));
                    dealOfTheMomentChanged = false;
                }
                customerFlow(dbHelper);
            }
        }

    }

    public static Modal.Customer loginFlow(DBHelper dbHelper){
        int choice = Utils.promptChoice(List.of("Login", "Register", "Exit"));
        switch(choice){
            case 0:
                // login
                String emailId = Utils.promptString("Enter username/emailId: ");
                if(Utils.isAdminUsername(emailId)){
                    String pwd = Utils.promptString("Enter password: ");
                    if(Utils.isAdmin(emailId, pwd)){
                        System.out.println("admin mode");
                        isAdmin = true;
                        return null;
                    } else {
                        System.out.println("user dosent exists");
                    }
                }
                Modal.Customer curCustomer = dbHelper.getCustomer(emailId);
                if (curCustomer == null) {
                    System.out.println("user dosent exists");
                    return null;
                }
                String pwd = Utils.promptString("Enter password: ");
                boolean valid = curCustomer.validatePassword(pwd);
                if(!valid){
                    System.out.println("username/password is wrong");
                    return null;
                }

                // load invoices
                if(Files.exists(Path.of(INVOICE_PROTO))) {
                    try (InputStream is = Files.newInputStream(Path.of(INVOICE_PROTO), StandardOpenOption.CREATE)) {
                        Invoice invoice;
                        while((invoice = Invoice.parseDelimitedFrom(is)) != null){
                            if (invoice.getEmail().equals(curCustomer.getEmail())){
                                userInvoices.add(invoice);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("user is authenticated");
                return curCustomer;
            case 1:
                // register
                String newEmailId = Utils.promptString("Enter username/emailId: ");
                String newName = Utils.promptString("Enter name: ");
                String newMobile = Utils.promptString("Enter mobileNo: ");
                String newPwd = Utils.promptString("Enter password: ");
                String newPwd2 = Utils.promptString("Enter confirm password: ");
                if (!Objects.equals(newPwd, newPwd2)){
                    System.out.println("passwords don't match");
                    return null;
                }
                Modal.Customer newCustomer = new Modal.Customer(newEmailId, "", newName, newMobile);
                newCustomer.setPassword(newPwd);
                if(Utils.checkPwdStrength(newPwd)){
                    customer = newCustomer;
                    dbHelper.addCustomer(customer);
                    if(pwdHistory.containsKey(customer.getEmail())){
                        List<String> userPwdHistoryList = pwdHistory.get(customer.getEmail());
                        if(userPwdHistoryList.contains(newPwd)){
                            System.out.println("password cannot be the same as previous 3 passwords");
                            return null;
                        }
                        Collections.rotate(userPwdHistoryList, 1);
                        userPwdHistoryList.set(0, newPwd);
                    } else {
                        List<String> userPwdHistoryList = new ArrayList<>();
                        userPwdHistoryList.add(newPwd);
                        userPwdHistoryList.add("");
                        userPwdHistoryList.add("");
                        pwdHistory.put(customer.getEmail(), userPwdHistoryList);
                    }
                    Utils.savePwdHistory(pwdHistory);
                    System.out.println("new user account created. you can use the account now");
                } else {
                    System.out.println("low password strength");
                    return null;
                }
                return newCustomer;
            case 3:
                System.exit(0);
        }
        return null;
    }

    public static void customerFlow(DBHelper dbHelper){
        int choice = Utils.promptChoice(List.of("Shop", "View Cart", "Checkout", "View invoices", "Change password", "Exit"));
        switch(choice){
            case 0:
                // shop
                // select category
                while(true) {
                    List<String> categories = List.of("Mobile", "Laptop", "Tablet", "Go back");
                    int categoryChoice = Utils.promptChoice(categories);
                    if (categoryChoice == 3) return;
                    List<Modal.Item> items;
                    items = dbHelper.queryItems("WHERE category='" + categories.get(categoryChoice) + "'");
                    Modal.Item item = Utils.promptItems(items);
                    if(item == null) continue;

                    String itemKey = item.getItemKey();
                    if(item.getStock() == 0) {
                        System.out.println("no stock");
                        continue;
                    }
                    if(numInCart.containsKey(itemKey)){
                        if(numInCart.get(itemKey)+1>item.getStock()){
                            System.out.println("cannot add any more item");
                            continue;
                        }
                        numInCart.merge(item.getItemKey(), 1, Integer::sum);
                    } else {
                        numInCart.put(itemKey, 1);
                        cart.add(item);
                    }

                    System.out.println("item added to cart");
                }
            case 1:
                // view cart
                Utils.printCartItems(cart, numInCart);
                break;
            case 2:
                // Checkout
                // check discount eligibility
                if(cart.size() < 1) return;
                int numPurchases = userInvoices.size();
                int totalCartValue = Utils.getCartValue(cart, numInCart, dealOfTheMoment);
                if(numPurchases == 4 | totalCartValue >= 20_000) {
                    userDiscountCode = Utils.generateDiscountCode();
                    dbHelper.updateDiscountCode(customer, userDiscountCode);
                } else if ((userDiscountCode = dbHelper.getDiscountCode(customer)) != null){
                    // existing discount code is valid
                } else {
                    // no valid discount code
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
                for(int i=0;i<cart.size();i++){
                    Modal.Item item = cart.get(i);
                    Item itemProtos = Item.newBuilder()
                            .setCategory(item.getCategory())
                            .setBrand(item.getBrand())
                            .setModel(item.getModel())
                            .setPrice(item.getPrice())
                            .setQuantity(numInCart.get(item.getItemKey()))
                            .build();
                    invoiceBuilder.addItems(itemProtos);
                }
                Invoice invoice = invoiceBuilder.build();

                try(OutputStream os = Files.newOutputStream(Path.of(INVOICE_PROTO), StandardOpenOption.CREATE, StandardOpenOption.APPEND)){
                    invoice.writeDelimitedTo(os);
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                // update stocks in db
                for(Modal.Item item: cart){
                    dbHelper.updateStock(item, item.getStock()-numInCart.get(item.getItemKey()));
                }
                if(userDiscountCode != null){
                    dbHelper.updateDiscountCodeExpiry(customer);
                }
                dealOfTheMoment = Utils.getMaxStockItem(dbHelper.queryItems(""));
                dealOfTheMomentChanged = true;
                cart.clear();
                numInCart.clear();
                userInvoices.add(invoice);
                break;
            case 3:
                // view invoices
                Utils.printInvoices(userInvoices);
                break;
            case 4:
                // change password
                String newPwd = Utils.promptString("Enter new password: ");
                String newPwd2 = Utils.promptString("Enter new confirm password: ");
                if (!Objects.equals(newPwd, newPwd2)){
                    System.out.println("passwords don't match");
                    return;
                }
                if(Utils.checkPwdStrength(newPwd)){
                    dbHelper.updateCustomerPwd(customer, newPwd);
                    if(pwdHistory.containsKey(customer.getEmail())){
                        List<String> userPwdHistoryList = pwdHistory.get(customer.getEmail());
                        if(userPwdHistoryList.contains(newPwd)){
                            System.out.println("password cannot be the same as previous 3 passwords");
                            return;
                        }
                        Collections.rotate(userPwdHistoryList, 1);
                        userPwdHistoryList.set(0, newPwd);
                    } else {
                        List<String> userPwdHistoryList = new ArrayList<>();
                        userPwdHistoryList.add(newPwd);
                        userPwdHistoryList.add("");
                        userPwdHistoryList.add("");
                        pwdHistory.put(customer.getEmail(), userPwdHistoryList);
                    }
                    Utils.savePwdHistory(pwdHistory);
                    System.out.println("password changed");
                } else {
                    System.out.println("low password strength");
                    return;
                }
                break;
            case 5:
                // exit
                System.exit(0);
        }

    }
    public static void adminFlow(DBHelper dbHelper){
        int choice;
        // force default password change
        if(Utils.isAdminDefaultEncryptedPwd(customer.getEncryptedPwd())){
            System.out.println("change default password");
            choice = 1;
        } else {
            choice = Utils.promptChoice(List.of("List low stock items", "Change password", "Exit"));
        }
        switch(choice){
            case 0:
                // list low stock items
                int threshold = Utils.promptInt("Enter stock threshold: ");
                List<Modal.Item> items = dbHelper.queryItems("WHERE stock <= "+ threshold + ";");
                Modal.Item item = Utils.promptItems(items);
                if(item == null) return;
                int newStock = Utils.promptInt("Enter stock to be added: ");
                dbHelper.updateStock(item, newStock);
                System.out.println("stock updated");
                break;
            case 1:
                // change password
                String newPwd = Utils.promptString("Enter new password: ");
                String newPwd2 = Utils.promptString("Enter new confirm password: ");
                if (!Objects.equals(newPwd, newPwd2)){
                    System.out.println("passwords don't match");
                    return;
                }
                if(Utils.checkPwdStrength(newPwd)){
                    customer.setPassword(newPwd);
                    Utils.updateAdminPwd(newPwd);
                    if(pwdHistory.containsKey(customer.getEmail())){
                        List<String> userPwdHistoryList = pwdHistory.get(customer.getEmail());
                        Collections.rotate(userPwdHistoryList, 1);
                        userPwdHistoryList.set(0, newPwd);
                    } else {
                        List<String> userPwdHistoryList = new ArrayList<>();
                        userPwdHistoryList.add(newPwd);
                        userPwdHistoryList.add("");
                        userPwdHistoryList.add("");
                        pwdHistory.put(customer.getEmail(), userPwdHistoryList);
                    }
                    Utils.savePwdHistory(pwdHistory);
                    System.out.println("password changed");
                } else {
                    System.out.println("low password strength");
                    return;
                }
                break;
            case 2:
                // exit
                System.exit(0);
        }
    }
}