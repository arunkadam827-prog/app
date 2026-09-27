package com.example.farmer.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Model class representing an item in the shopping cart.
 * Fields match the JSON response from the Spring Boot CartController,
 * supporting both flat and nested Product representations.
 */
public class CartItem implements Serializable {

    @SerializedName(value = "cartItemId", alternate = {"id"})
    private Long cartItemId;

    @SerializedName("product")
    private Product product;

    @SerializedName("productName")
    private String productName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    // Default constructor required for Gson
    public CartItem() {
    }

    public CartItem(Long cartItemId, String productName, int quantity, double price) {
        this.cartItemId = cartItemId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductName() {
        if (productName != null && !productName.trim().isEmpty()) {
            return productName;
        }
        if (product != null && product.getProductName() != null) {
            return product.getProductName();
        }
        return "";
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        if (price > 0) {
            return price;
        }
        if (product != null) {
            return product.getPrice();
        }
        return 0.0;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
