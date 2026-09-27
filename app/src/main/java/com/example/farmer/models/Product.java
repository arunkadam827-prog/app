package com.example.farmer.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Product model representing a product for the remote PostgreSQL backend.
 * Room annotations have been removed as the app now uses a remote database.
 */
public class Product implements Serializable {

    @SerializedName(value = "productId", alternate = {"id"})
    public int productId;

    // Seller information
    @SerializedName(value = "farmerId", alternate = {"sellerId"})
    public int farmerId;

    @SerializedName(value = "farmerName", alternate = {"sellerName"})
    public String farmerName;

    // Product information
    @SerializedName("productName")
    public String productName;

    @SerializedName("description")
    public String description;

    @SerializedName("price")
    public double price;

    @SerializedName("quantityAvailable")
    public int quantityAvailable;

    @SerializedName("category")
    public String category;

    @SerializedName("imageUrl")
    public String imageUrl;

    // Timestamps
    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    // Required empty constructor for Gson and Retrofit
    public Product() {
    }

    public Product(
            int farmerId,
            String farmerName,
            String productName,
            String description,
            double price,
            int quantityAvailable,
            String category,
            String imageUrl
    ) {
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.category = category;
        this.imageUrl = imageUrl;

        String now = String.valueOf(System.currentTimeMillis());
        this.createdAt = now;
        this.updatedAt = now;
    }

    // --- Getters and Setters ---
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    public String getCategory() {
        if (category == null || category.trim().isEmpty()) {
            return inferCategory(productName);
        }
        return category;
    }

    private static String inferCategory(String name) {
        if (name == null) return "Vegetables";
        String lower = name.toLowerCase();
        if (lower.contains("apple") || lower.contains("banana") || lower.contains("mango")
                || lower.contains("orange") || lower.contains("grape") || lower.contains("guava")
                || lower.contains("papaya") || lower.contains("pineapple") || lower.contains("watermelon")
                || lower.contains("fruit") || lower.contains("berry")) {
            return "Fruits";
        }
        if (lower.contains("milk") || lower.contains("paneer") || lower.contains("cheese")
                || lower.contains("butter") || lower.contains("ghee") || lower.contains("curd")
                || lower.contains("dairy")) {
            return "Dairy";
        }
        if (lower.contains("wheat") || lower.contains("rice") || lower.contains("grain")
                || lower.contains("dal") || lower.contains("pulse") || lower.contains("flour")) {
            return "Grains";
        }
        return "Vegetables";
    }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}