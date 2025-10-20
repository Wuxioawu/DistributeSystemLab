package com.peng.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.time.Instant;


/**
 * UserProfile entity for Redis Cluster
 */
@Data
public class UserProfile {
    private String userId;
    private String username;
    private String email;
    private String lastLoginTime;

    // Jackson ObjectMapper for JSON serialization/deserialization
    private static final ObjectMapper mapper = new ObjectMapper();

    public UserProfile() {}

    public UserProfile(String userId, String username, String email, String lastLoginTime) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.lastLoginTime = lastLoginTime != null ? lastLoginTime : Instant.now().toString();
    }

    // Convert to JSON string
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize UserProfile", e);
        }
    }

    // Convert from JSON string
    public static UserProfile fromJson(String json) {
        try {
            return mapper.readValue(json, UserProfile.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize UserProfile", e);
        }
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", lastLoginTime='" + lastLoginTime + '\'' +
                '}';
    }
}

