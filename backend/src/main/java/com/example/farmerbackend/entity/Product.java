package com.example.farmerbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(nullable = false)
    private String productName;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int quantityAvailable;

    private String category;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Product() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }

    public Long getFarmerId() {
        return farmer != null ? farmer.getUserId() : null;
    }

    public String getFarmerName() {
        return farmer != null ? farmer.getFullName() : "Kisan Producer";
    }

    public String getFarmerPhone() {
        return farmer != null && farmer.getPhone() != null && !farmer.getPhone().isEmpty() ? farmer.getPhone() : "9356601104";
    }

    public String getFarmerCity() {
        return farmer != null && farmer.getCity() != null && !farmer.getCity().isEmpty() ? farmer.getCity() : "Direct Farm";
    }

    public String getFarmerAddress() {
        return farmer != null && farmer.getAddress() != null ? farmer.getAddress() : "Farm Direct Sourcing";
    }

    public boolean isDirectFromFarm() {
        return farmer != null;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}