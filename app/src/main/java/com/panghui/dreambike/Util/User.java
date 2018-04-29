package com.panghui.dreambike.Util;

import android.support.annotation.BinderThread;

import java.io.Serializable;
import java.util.Observable;

public class User implements Serializable {

    private String username;
    private String email;
    private String password;
    private String balance;

    private static class UserHolder{
        private static final User INSTANCE = new User();
    }

    private User(){}

    public static final User getInstance(){
        return UserHolder.INSTANCE;
    }

    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getUsername(){
        return username;
    }

    public String getBalance(){
        return balance;
    }

    public void setUsername(String username){
        this.username=username;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public void setPassword(String password){
        this.password=password;
    }
    public void setBalance(String balance){
        this.balance=balance;
    }
}
