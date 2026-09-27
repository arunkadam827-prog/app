package com.example.farmerbackend.repository;

import com.example.farmerbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByProductIdDesc();
    List<Product> findByFarmerUserIdOrderByProductIdDesc(Long farmerId);
    List<Product> findByCategoryIgnoreCaseOrderByProductIdDesc(String category);
    List<Product> findByProductNameContainingIgnoreCaseOrderByProductIdDesc(String productName);
}