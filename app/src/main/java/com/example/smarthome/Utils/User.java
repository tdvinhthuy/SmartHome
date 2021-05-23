package com.example.smarthome.Utils;

public class User {
    private String email;
    private String fullname;
    private String phone;
    private String password;

    public User() {
        email = "";
        fullname = "";
        phone = "";
        password = "";
    }

    public User(String email, String fullname, String phone, String password) {
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPassword() {
        return password;
    }
}
