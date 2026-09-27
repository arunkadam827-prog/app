package com.example.farmerbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class CustomerOrder {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long orderId;

 @ManyToOne(fetch = FetchType.EAGER)
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 private double totalAmount;
 private String status;
 private String paymentMethod;
 private String deliveryAddress;
 private LocalDateTime orderDate;

 @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
 private List<OrderItem> items = new ArrayList<>();

 @PrePersist
 public void onCreate() {
  orderDate = LocalDateTime.now();
  if (status == null) {
   status = "PLACED";
  }
 }

 public CustomerOrder() {}

 public Long getOrderId() { return orderId; }
 public void setOrderId(Long orderId) { this.orderId = orderId; }

 public User getUser() { return user; }
 public void setUser(User user) { this.user = user; }

 public double getTotalAmount() { return totalAmount; }
 public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

 public String getStatus() { return status; }
 public void setStatus(String status) { this.status = status; }

 public LocalDateTime getOrderDate() { return orderDate; }

 public List<OrderItem> getItems() { return items; }
 public void setItems(List<OrderItem> items) { this.items = items; }

 public Long getId() { return orderId; }
 public int getQuantity() { return items != null && !items.isEmpty() ? items.stream().mapToInt(OrderItem::getQuantity).sum() : 1; }
 public String getDeliveryAddress() {
  if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
   return deliveryAddress;
  }
  return user != null && user.getAddress() != null ? user.getAddress() : "Standard Delivery";
 }
 public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
 public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "UPI"; }
 public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
 public double getTotalPrice() { return totalAmount; }
 public String getOrderStatus() { return status; }

 public String getProductName() {
  if (items != null && !items.isEmpty() && items.get(0).getProduct() != null) {
   String first = items.get(0).getProduct().getProductName();
   if (items.size() > 1) {
    return first + " (+" + (items.size() - 1) + " more)";
   }
   return first;
  }
  return "Farm Fresh Products";
 }

 public Long getFarmerId() {
  if (items != null && !items.isEmpty() && items.get(0).getProduct() != null) {
   return items.get(0).getProduct().getFarmerId();
  }
  return null;
 }

 public Long getProductId() {
  if (items != null && !items.isEmpty() && items.get(0).getProduct() != null) {
   return (long) items.get(0).getProduct().getProductId();
  }
  return null;
 }
}