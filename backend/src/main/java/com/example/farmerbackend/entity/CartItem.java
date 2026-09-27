package com.example.farmerbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class CartItem {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long cartItemId;

 @ManyToOne(fetch = FetchType.EAGER)
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @ManyToOne(fetch = FetchType.EAGER)
 @JoinColumn(name = "product_id", nullable = false)
 private Product product;

 private int quantity;

 public CartItem() {}

 public Long getCartItemId() { return cartItemId; }
 public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

 public User getUser() { return user; }
 public void setUser(User user) { this.user = user; }

 public Product getProduct() { return product; }
 public void setProduct(Product product) { this.product = product; }

 public int getQuantity() { return quantity; }
 public void setQuantity(int quantity) { this.quantity = quantity; }

 public String getProductName() { return product != null ? product.getProductName() : null; }
 public double getPrice() { return product != null ? product.getPrice() : 0.0; }
}