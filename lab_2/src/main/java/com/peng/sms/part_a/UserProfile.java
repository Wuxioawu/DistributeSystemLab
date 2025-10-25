package com.peng.sms.part_a;

import com.google.gson.Gson;

import java.time.Instant;

public class UserProfile {
    private String userId;
    private String username;
    private String email;
    private long lastLoginTime;
    private int version;

    public UserProfile(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.lastLoginTime = Instant.now().toEpochMilli();
        this.version = 1;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.version++;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.version++;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void updateLastLogin() {
        this.lastLoginTime = Instant.now().toEpochMilli();
        this.version++;
    }

    public int getVersion() {
        return version;
    }

    // Serialize to JSON
    public String toJson() {
        return new Gson().toJson(this);
    }

    // Deserialize from JSON
    public static UserProfile fromJson(String json) {
        return new Gson().fromJson(json, UserProfile.class);
    }

    @Override
    public String toString() {
        return String.format("UserProfile{userId='%s', username='%s', email='%s', version=%d, lastLogin=%d}",
                userId, username, email, version, lastLoginTime);
    }
}