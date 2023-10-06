package org.example;

import java.util.Objects;

public class Modal {
    static class Customer {
        private String email;
        private String encryptedPwd;
        private String name;
        private String mobile;


        public String getEmail() {
            return email;
        }

        public String getEncryptedPwd() {
            return encryptedPwd;
        }

        public String getName() {
            return name;
        }

        public String getMobile() {
            return mobile;
        }

        public Customer(String email, String encryptedPwd, String name, String mobile) {
            this.email = email;
            this.encryptedPwd = encryptedPwd;
            this.name = name;
            this.mobile = mobile;
        }

        public static String encryptPwd(String pwd){
            StringBuilder stringBuilder = new StringBuilder();
            for(char c: pwd.toCharArray()){
                if (c == 'z'){
                    stringBuilder.append('a');
                } else if (c == 'Z'){
                    stringBuilder.append('A');
                } else if (c == '9'){
                    stringBuilder.append('0');
                } else {
                    stringBuilder.append((char) (c + 1));
                }
            }
            return stringBuilder.toString();
        }

        public void setPassword(String pwd){
            this.encryptedPwd = encryptPwd(pwd);
        }

        public boolean validatePassword(String pwd){
            return Objects.equals(this.encryptedPwd, encryptPwd(pwd));
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }

    static class Item {
        private String category;
        private String brand;
        private String model;
        private int price;
        private int stock;

        public Item(String category, String brand, String model, int price, int stock) {
            this.category = category;
            this.brand = brand;
            this.model = model;
            this.price = price;
            this.stock = stock;
        }

        public void addStock(int num){
            this.stock += num;
        }

        public void removeStock(int num){
            this.stock -= num;
        }

        public void setPrice(int price){
            this.price = price;
        }

        public void setStock(int stock){
            this.stock = stock;
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

        public String getItemKey(){
            return this.brand +"-"+ this.model;
        }
    }
}



