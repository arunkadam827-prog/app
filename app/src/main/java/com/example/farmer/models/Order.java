package com.example.farmer.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Order model representing an order for the remote PostgreSQL backend.
 */
public class Order implements Serializable {

    @SerializedName(value = "orderId", alternate = {"id"})
    public int orderId;

    @SerializedName(value = "buyerId", alternate = {"userId"})
    public int buyerId;

    @SerializedName("farmerId")
    public int farmerId;

    @SerializedName("productId")
    public int productId;

    @SerializedName("quantity")
    public int quantity;

    @SerializedName(value = "totalPrice", alternate = {"totalAmount"})
    public double totalPrice;

    @SerializedName(value = "orderStatus", alternate = {"status"})
    public String orderStatus;

    @SerializedName(value = "deliveryAddress", alternate = {"address"})
    public String deliveryAddress;

    @SerializedName(value = "paymentMethod", alternate = {"payment_method"})
    public String paymentMethod;

    @SerializedName(value = "createdAt", alternate = {"orderDate"})
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    public Order() {
    }

    public Order(int buyerId, int farmerId, int productId, int quantity,
                 double totalPrice, String deliveryAddress) {
        this.buyerId = buyerId;
        this.farmerId = farmerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.orderStatus = "PENDING";
    }

    // Getters and Setters ...
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "UPI"; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
