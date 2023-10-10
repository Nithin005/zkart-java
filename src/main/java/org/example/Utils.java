package org.example;

import org.example.protos.Invoice;
import org.example.protos.InvoiceItem;
import org.example.protos.Invoices;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    private static final Scanner sc = new Scanner(System.in);

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

//    public static void printCustomers(List<Customer> customers) {
//        System.out.printf("%5s %20s %20s %15s %15s\n", "index", "EmailId", "EncryptedPwd", "Name", "Mobile");
//        for (int i = 0; i < customers.size(); i++) {
//            Customer customer = customers.get(i);
//            System.out.printf("%5d %20s %20s %15s %15s\n", i + 1, customer.getEmail(), customer.getEncryptedPwd(), customer.getName(), customer.getMobile());
//        }
//    }

    public static int promptChoice(List<String> choices) {
        for (int i = 0; i < choices.size(); i++) {
            System.out.printf("%5d %s\n", i + 1, choices.get(i));
        }
        int choice = promptInt("Enter choice: ");
        return choice - 1;
    }

    public static String promptString(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return sc.next();
            } catch (InputMismatchException e) {
                System.out.println("input invalid");
                sc.nextLine();
            }
        }
    }

    public static String promptStringLine(String prompt) {
        Scanner in = new Scanner(System.in);
        in.useDelimiter("\n");
        while (true) {
            System.out.print(prompt);
            try {
                return in.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("input invalid");
                sc.nextLine();
            }
        }
    }

    public static int promptInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return sc.nextInt();
            } catch (Exception e) {
                System.out.println("input invalid");
                sc.nextLine();
            }

        }
    }

    public static boolean promptBoolean(String prompt) {
        while (true){
            System.out.print(prompt);
            try {
                String input = sc.next();
                return input.equals("Y") | input.equals("y");
            } catch (InputMismatchException e) {
                System.out.println("input invalid");
                sc.nextLine();
            }
        }

    }

    public static String promptEmail(String prompt){
        // RFC 5322
        String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        while(true){
            String testEmail = promptString(prompt);
            if(patternMatches(testEmail, regexPattern)){
                return testEmail;
            } else {
                System.out.println("not a valid email");
            }
        }
    }

    public static String promptMobileNo(String prompt){
        String regexPattern = "^\\+?[1-9][0-9]{7,14}$";
        while(true){
            String testPhoneNo = promptString(prompt);
            if(patternMatches(testPhoneNo, regexPattern)){
                return testPhoneNo;
            } else {
                System.out.println("not a valid phone no");
            }
        }
    }

    public static String promptChangePassword(){
        while (true) {
            String newPwd = Utils.promptString("Enter new password: ");
            // 1. check password strength
            if(!checkPwdStrength(newPwd)) {
                System.out.println("password strength low");
                continue;
            }
            String newPwd2 = Utils.promptString("Enter new confirm password: ");
            if (!newPwd.equals(newPwd2)) {
                System.out.println("passwords don't match");
                continue;
            }
            return newPwd;
        }
    }


    public static String generateInvoiceNumber() {
        Random rand = new Random();
        int min = 10000;
        int max = 65534;
        int invoiceNumber = rand.nextInt((max - min) + 1) + min;
        return String.valueOf(invoiceNumber);
    }

    public static int getCartValue(List<Item> items) {
        int sum = 0;
        for (Item item : items) {
            sum += item.getPrice() * item.getQuantity();
        }
        return sum;
    }

    public static int getCartValueWithDiscount(List<Item> items, Item dealOfTheMoment) {
        int sum = 0;
        for (Item item : items) {
            if (item.getItemId() == dealOfTheMoment.getItemId()) {
                // dealOfTheMoment
                sum += item.getPrice() * item.getQuantity() * 90 / 100;
            } else {
                sum += item.getPrice() * item.getQuantity();
            }
        }
        return sum;
    }



    public static void printInvoices(List<Invoice> invoices) {
        for (Invoice invoice : invoices) {
            System.out.println("-".repeat(60));
            System.out.println("Invoice Number: " + invoice.getInvoiceNo());
            System.out.println("Date: " + invoice.getDatetime());
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

    public static float generateDiscountValue() {
        Random rand = new Random();
        int min = 20;
        int max = 30;
//        return min + rand.nextInt(max - min + 1);
        return min + rand.nextFloat() * (max - min);
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

    @SuppressWarnings("unchecked")
    public static Map<Integer, List<String>> readPwdHistory(Path pwdHistoryFile) {
        if (Files.exists(pwdHistoryFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(pwdHistoryFile))) {
                return (Map<Integer, List<String>>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new HashMap<>();
    }

    public static void savePwdHistory(Path pwdHistoryFile, Map<Integer, List<String>> pwdHistory) {
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
        List<Invoice> invoicesList;
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.CREATE)){
            invoicesList = Invoices.parseFrom(is).getInvoicesList();
        } catch (NoSuchFileException e){
            invoicesList = new ArrayList<>();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        try (OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
            Invoices invoices = Invoices.newBuilder().addAllInvoices(invoicesList).addInvoices(invoice).build();
            invoices.writeTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Invoice> readInvoices(Customer customer, Path invoiceProtoPath){
        List<Invoice> userInvoices = new ArrayList<>();
        if(Files.exists(invoiceProtoPath)) {
            try (InputStream is = Files.newInputStream(invoiceProtoPath, StandardOpenOption.CREATE)) {
                Invoices invoices = Invoices.parseFrom(is);
                for(Invoice invoice: invoices.getInvoicesList()){
                    if(invoice.getCustomerId() == customer.getCustomerId()){
                        userInvoices.add(invoice);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return userInvoices;
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }

}
