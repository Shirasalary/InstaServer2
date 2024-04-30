package com.ashcollege.entities;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String password;
    private String token;
    private String pictureUrl;


    public User(){
    }

    public User(int id, String username, String password, String token, String pictureUrl) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.token = token;
        this.pictureUrl = pictureUrl;
    }

    public User(int id ,String username) {

        this.username = username;
        this.id = id;
    }
    public User(String username, String password) {

        this.username = username;
        this.password = password;
    }

    public User(int id ,String username, String pictureUrl) {

        this.username = username;
        this.id = id;
        this.pictureUrl = pictureUrl;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

}
