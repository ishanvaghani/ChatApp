package com.chatapp.database;

public class User {
    private String name;
    private String pass;
    private String email;
    private String image;
    private String id;
    private String status;
    private String userStatus;
    private boolean isBlocked = false;
    private String search;
    private String typingTo;

    public User(String name, String pass, String email, String id, String image, String status, String userStatus, boolean isBlocked, String search, String typingTo) {
        this.name = name;
        this.pass = pass;
        this.email = email;
        this.id = id;
        this.image = image;
        this.status = status;
        this.userStatus = userStatus;
        this.isBlocked = isBlocked;
        this.search = search;
        this.typingTo = typingTo;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getName() {
        return name;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
