package org.example;

import org.example.protos.Invoice;
import org.example.protos.InvoiceItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Utils {
    private static Scanner sc = new Scanner(System.in);

    public static Item promptItems(List<Item> items) {
        printItems(items);
        int choice = promptInt("Enter choice(0 to quit): ");
        if (choice == 0) return null;
        return items.get(choice - 1);
    }

    public static void printItems(List<Item> items) {
        System.out.printf("%5s %10s %10s %10s %10s %10s\n", "index", "Category", "Brand", "Model", "Price", "Stock");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.printf("%5d %10s %10s %10s %10d %10d\n", i + 1, item.getCategory(), item.getBrand(), item.getModel(), item.getPrice(), item.getStock());
        }
        System.out.println("-".repeat(60));
    }

    public static void printCartItems(List<Item> items) {
        System.out.printf("%5s %10s %10s %10s %10s %10s\n", "index", "Category", "Brand", "Model", "Price", "Quantity");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.printf("%5d %10s %10s %10s %10d %10d\n", i + 1, item.getCategory(), item.getBrand(), item.getModel(), item.getPrice(), item.getQuantity());
        }
        System.out.println("-".repeat(60));
    }

    public static void printCustomers(List<Customer> customers) {
        System.out.printf("%5s %20s %20s %15s %15s\n", "index", "EmailId", "EncryptedPwd", "Name", "Mobile");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            System.out.printf("%5d %20s %20s %15s %15s\n", i + 1, customer.getEmail(), customer.getEncryptedPwd(), customer.getName(), customer.getMobile());
        }
    }

    public static int promptChoice(List<String> choices) {
        for (int i = 0; i < choices.size(); i++) {
            System.out.printf("%5d %s\n", i + 1, choices.get(i));
        }
        int choice = promptInt("Enter choice: ");
        return choice - 1;
    }

    public static String promptString(String prompt) {
        System.out.print(prompt);
        return sc.next();
    }

    public static int promptInt(String prompt) {
        System.out.print(prompt);
        return sc.nextInt();
    }

    public static boolean promptBoolean(String prompt) {
        System.out.print(prompt);
        String input = sc.next();
        return input.equals("Y") | input.equals("y");
    }

    public static String promptChangePassword(){
        String newPwd = Utils.promptString("Enter new password: ");
        String newPwd2 = Utils.promptString("Enter new confirm password: ");
        if (!Objects.equals(newPwd, newPwd2)){
            System.out.println("passwords don't match");
            return null;
        }
        return newPwd;
    }


    public static String generateInvoiceNumber() {
        Random rand = new Random();
        int min = 10000;
        int max = 65534;
        int invoiceNumber = rand.nextInt((max - min) + 1) + min;
        return String.valueOf(invoiceNumber);
    }

    public static int getCartValue(List<Item> items, Item dealOfTheMoment) {
        int sum = 0;
        for (Item item : items) {
            if (Objects.equals(item.getItemKey(), dealOfTheMoment.getItemKey())) {
                // dealOfTheMoment
                System.out.println("Deal of the moment discount added (10%)");
                sum += item.getPrice() * item.getQuantity() * 90 / 100;
            } else {
                sum += item.getPrice() * item.getQuantity();
            }
        }
        return sum;
    }


//    public static List<Invoice> filterInvoices(List<Invoice> invoices, String emailId){
//        return invoices.stream().filter((e)-> e.getEmail().equals(emailId)).collect(Collectors.toList());
//    }

    public static void printInvoices(List<Invoice> invoices) {
        for (Invoice invoice : invoices) {
            System.out.println("-".repeat(60));
            System.out.println("Invoice Number: " + invoice.getInvoiceNo());
            System.out.println("Date: " + invoice.getDatetime().toString());
            System.out.println();
            List<InvoiceItem> items = invoice.getItemsList();
            System.out.printf("%5s %10s %10s %10s %10s %10s\n", "index", "Category", "Brand", "Model", "Price", "Quantity");
            for (int i = 0; i < items.size(); i++) {
                InvoiceItem item = items.get(i);
                System.out.printf("%5d %10s %10s %10s %10d %10d\n", i + 1, item.getCategory(), item.getBrand(), item.getModel(), item.getPrice(), item.getQuantity());
            }
            System.out.println();
            System.out.println("Total value: " + invoice.getTotalValue());
            System.out.println("Discounted value: " + invoice.getDiscountedValue());
            System.out.println("-".repeat(60));
        }

    }

    public static boolean isAdmin(String emailId, String password) {
        String adminEncryptedPwd = getAdminEncryptedPwd();
        return Objects.equals(emailId, "admin@zoho.com") && Objects.equals(encryptPwd(password), adminEncryptedPwd);

    }

    public static String getAdminEncryptedPwd() {
        Path adminFile = Path.of("admin.dat");
        if (Files.exists(adminFile)) {
            try (BufferedReader reader = Files.newBufferedReader(adminFile)) {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String initialPassword = "xyzzy";
            updateAdminPwd(initialPassword);
            return encryptPwd(initialPassword);
        }
    }

    public static boolean isAdminDefaultEncryptedPwd(String encryptedPwd) {
        return Objects.equals(encryptPwd("xyzzy"), encryptedPwd);
    }

    public static void updateAdminPwd(String password) {
        Path adminFile = Path.of("admin.dat");
        try (BufferedWriter writer = Files.newBufferedWriter(adminFile)) {
            writer.write(encryptPwd(password));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateDiscountCode() {
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = 6;

        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

    public static int generateDiscountValue() {
        Random rand = new Random();
        int min = 20;
        int max = 30;
        return min + rand.nextInt(max - min + 1);
    }

    public static Item getMaxStockItem(List<Item> items) {
        Item max = null;
        int stockMax = 0;
        for (Item item : items) {
            if (item.getStock() > stockMax) {
                max = item;
                stockMax = item.getStock();
            }
        }
        return max;
    }

    public static boolean checkPwdStrength(String pwd) {
        int lowerCase = 0;
        int upperCase = 0;
        if (pwd.length() < 6) {
            System.out.println("password too short");
            return false;
        }
        for (char c : pwd.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                upperCase++;
            } else if (c >= 'a' && c <= 'z') {
                lowerCase++;
            }
        }
        if (lowerCase < 2) {
            System.out.println("password must contain at least 2 lowercase letters");
            return false;
        } else if (upperCase < 2) {
            System.out.println("password must contain at least 2 uppercase letters");
            return false;
        }
        return true;
    }

    public static Map<String, List<String>> readPwdHistory(Path pwdHistoryFile) {
        if (Files.exists(pwdHistoryFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(pwdHistoryFile))) {
                Map<String, List<String>> pwdHistory = (Map<String, List<String>>) ois.readObject();
                return pwdHistory;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new HashMap<>();
    }

    public static void savePwdHistory(Path pwdHistoryFile, Map<String, List<String>> pwdHistory) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(pwdHistoryFile))) {
            oos.writeObject(pwdHistory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanFiles() {
        Path adminFile = Path.of("admin.dat");
        Path pwdHistoryFile = Path.of("pwdHistory.dat");
        try {
            Files.deleteIfExists(adminFile);
            Files.deleteIfExists(pwdHistoryFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String encryptPwd(String pwd) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : pwd.toCharArray()) {
            if (c == 'z') {
                stringBuilder.append('a');
            } else if (c == 'Z') {
                stringBuilder.append('A');
            } else if (c == '9') {
                stringBuilder.append('0');
            } else {
                stringBuilder.append((char) (c + 1));
            }
        }
        return stringBuilder.toString();
    }

    public static void saveInvoice(Invoice invoice, Path path) {
        try (OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            invoice.writeDelimitedTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
