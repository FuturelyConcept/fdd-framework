package com.fdd.demo.domain;

/**
 * Request to create an order
 */
public class CreateOrderRequest {
    private UserData userData;
    private String productId;
    private int quantity;

    public CreateOrderRequest() {}

    public CreateOrderRequest(UserData userData, String productId, int quantity) {
        this.userData = userData;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and setters
    public UserData getUserData() { return userData; }
    public void setUserData(UserData userData) { this.userData = userData; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}