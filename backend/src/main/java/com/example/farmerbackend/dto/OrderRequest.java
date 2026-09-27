package com.example.farmerbackend.dto;

public class OrderRequest {
    private Long userId;
    private String deliveryAddress;
    private String paymentMethod;

    public OrderRequest() {}

    public OrderRequest(Long userId, String deliveryAddress, String paymentMethod) {
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
