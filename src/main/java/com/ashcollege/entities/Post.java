package com.ashcollege.entities;

import java.sql.Timestamp;

public class Post {
    private int id;
    private int userId;
    private String text;
    private Timestamp date;

    public Post(int id, int userId, String text, Timestamp date) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
