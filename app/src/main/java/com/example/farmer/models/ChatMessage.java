package com.example.farmer.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatMessage implements Serializable {

    @SerializedName("id")
    public Long id;

    @SerializedName("senderId")
    public Long senderId;

    @SerializedName("receiverId")
    public Long receiverId;

    @SerializedName("message")
    public String message;

    @SerializedName("productId")
    public Long productId;

    @SerializedName("productName")
    public String productName;

    @SerializedName("read")
    public boolean isRead;

    @SerializedName("createdAt")
    public String createdAt;

    public ChatMessage() {
    }

    public ChatMessage(Long senderId, Long receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}