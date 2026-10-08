package com.example.farmer.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Lead implements Serializable {

    @SerializedName("id")
    public Long id;

    @SerializedName("farmerId")
    public Long farmerId;

    @SerializedName("buyerId")
    public Long buyerId;

    @SerializedName("buyerName")
    public String buyerName;

    @SerializedName("buyerPhone")
    public String buyerPhone;

    @SerializedName("productId")
    public Long productId;

    @SerializedName("productName")
    public String productName;

    @SerializedName("quantity")
    public String quantity;

    @SerializedName("message")
    public String message;

    @SerializedName("status")
    public String status; // NEW, CONTACTED, CLOSED

    @SerializedName("createdAt")
    public String createdAt;

    public Lead() {}

    public Lead(Long farmerId, Long buyerId, String buyerName, String buyerPhone,
                Long productId, String productName, String quantity, String message) {
        this.farmerId = farmerId;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.message = message;
        this.status = "NEW";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
