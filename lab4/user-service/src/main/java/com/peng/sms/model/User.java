package com.peng.sms.model;

import jakarta.persistence.Entity;

@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String email;

    // constructors, getters, setters
    public User() {}
    public User(String name, String email) { this.name = name; this.email = email; }
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }
}