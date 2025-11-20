package com.peng.sms.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Order {
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String product;
    private int quantity;

    // constructors, getters, setters
    public Order() {
    }

    public Order(Long userId, String product, int quantity) {
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
    }


}
