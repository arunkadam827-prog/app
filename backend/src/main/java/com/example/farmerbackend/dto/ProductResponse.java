package com.example.farmerbackend.dto;
import com.example.farmerbackend.entity.Product;
import java.time.LocalDateTime;
public class ProductResponse {
 public Long productId,sellerId; public String sellerName,sellerType,productName,description,category,imageUrl; public double price; public int quantityAvailable; public LocalDateTime createdAt,updatedAt;
 public static ProductResponse of(Product p){ProductResponse r=new ProductResponse();r.productId=p.getProductId();r.sellerId=p.getFarmer().getUserId();r.sellerName=p.getFarmer().getFullName();r.sellerType=p.getFarmer().getUserType();r.productName=p.getProductName();r.description=p.getDescription();r.price=p.getPrice();r.quantityAvailable=p.getQuantityAvailable();r.category=p.getCategory();r.imageUrl=p.getImageUrl();r.createdAt=p.getCreatedAt();r.updatedAt=p.getUpdatedAt();return r;}
}
