package com.example.farmerbackend.service;

import com.example.farmerbackend.entity.Product;
import com.example.farmerbackend.entity.User;
import com.example.farmerbackend.repository.ProductRepository;
import com.example.farmerbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Product addProduct(Long farmerId, Product product) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        if (!"FARMER".equalsIgnoreCase(farmer.getUserType())) {
            throw new RuntimeException("Only farmers can add products");
        }

        product.setFarmer(farmer);
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByProductIdDesc();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getFarmerProducts(Long farmerId) {
        return productRepository.findByFarmerUserIdOrderByProductIdDesc(farmerId);
    }

    public List<Product> getByCategory(String category) {
        return productRepository.findByCategoryIgnoreCaseOrderByProductIdDesc(category);
    }

    public List<Product> search(String keyword) {
        return productRepository.findByProductNameContainingIgnoreCaseOrderByProductIdDesc(keyword);
    }

    public Product updateProduct(Long productId, Long farmerId, Product updatedProduct) {
        Product existing = getProduct(productId);

        if (!existing.getFarmer().getUserId().equals(farmerId)) {
            throw new RuntimeException("You cannot update this product");
        }

        existing.setProductName(updatedProduct.getProductName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantityAvailable(updatedProduct.getQuantityAvailable());
        existing.setCategory(updatedProduct.getCategory());
        existing.setImageUrl(updatedProduct.getImageUrl());

        return productRepository.save(existing);
    }

    public void deleteProduct(Long productId, Long farmerId) {
        Product product = getProduct(productId);

        if (!product.getFarmer().getUserId().equals(farmerId)) {
            throw new RuntimeException("You cannot delete this product");
        }

        productRepository.delete(product);
    }
}