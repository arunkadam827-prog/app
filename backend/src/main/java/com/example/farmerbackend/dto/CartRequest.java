package com.example.farmerbackend.dto;

public class CartRequest {
 private Long userId;
 private Long productId;
 private int quantity;

 public CartRequest() {}

 public Long getUserId() { return userId; }
 public void setUserId(Long userId) { this.userId = userId; }

 public Long getProductId() { return productId; }
 public void setProductId(Long productId) { this.productId = productId; }

 public int getQuantity() { return quantity; }
 public void setQuantity(int quantity) { this.quantity = quantity; }
}