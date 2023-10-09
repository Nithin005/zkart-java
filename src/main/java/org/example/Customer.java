package org.example;


public class Customer {
    private int customerId;
    private String email;
    private String encryptedPwd;
    private String name;
    private String mobile;
    private boolean isAdmin = false;

    public Customer(int customerId, String email, String encryptedPwd, String name, String mobile) {
        this.customerId = customerId;
        this.email = email;
        this.encryptedPwd = encryptedPwd;
        this.name = name;
        this.mobile = mobile;
    }

    public int getCustomerId() { return customerId; }

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

    public void setCustomerId(int customerId){
        this.customerId = customerId;
    }

    public void setAdmin(){
        this.isAdmin = true;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void setPassword(String pwd) {
        this.encryptedPwd = Utils.encryptPwd(pwd);
    }

    public boolean validatePassword(String pwd) {
        return this.encryptedPwd.equals(Utils.encryptPwd(pwd));
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
