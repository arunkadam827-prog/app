package com.example.farmerbackend.repository;

import com.example.farmerbackend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
 List<CartItem> findByUserUserId(Long userId);
 Optional<CartItem> findByUserUserIdAndProductProductId(Long userId, Long productId);
 void deleteByUserUserId(Long userId);
}