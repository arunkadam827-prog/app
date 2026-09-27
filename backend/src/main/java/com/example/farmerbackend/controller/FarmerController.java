package com.example.farmerbackend.controller;

import com.example.farmerbackend.entity.CustomerOrder;
import com.example.farmerbackend.entity.OrderItem;
import com.example.farmerbackend.entity.Product;
import com.example.farmerbackend.repository.CustomerOrderRepository;
import com.example.farmerbackend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmers")
@CrossOrigin(origins = "*")
public class FarmerController {

    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    public FarmerController(ProductRepository productRepository, CustomerOrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{farmerId}/stats")
    public ResponseEntity<?> getFarmerStats(@PathVariable Long farmerId) {
        List<Product> products = productRepository.findByFarmerUserIdOrderByProductIdDesc(farmerId);
        int productCount = products.size();

        List<CustomerOrder> allOrders = orderRepository.findAll();
        long orderCount = 0;
        double totalRevenue = 0.0;

        for (CustomerOrder order : allOrders) {
            boolean matchesFarmer = false;
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct() != null && item.getProduct().getFarmer() != null
                            && farmerId.equals(item.getProduct().getFarmer().getUserId())) {
                        matchesFarmer = true;
                        totalRevenue += item.getPrice() * item.getQuantity();
                    }
                }
            }
            if (matchesFarmer) {
                orderCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("productCount", productCount);
        stats.put("orderCount", orderCount);
        stats.put("totalRevenue", String.format("%.2f", totalRevenue));
        stats.put("avgRating", "4.8");

        return ResponseEntity.ok(stats);
    }
}
