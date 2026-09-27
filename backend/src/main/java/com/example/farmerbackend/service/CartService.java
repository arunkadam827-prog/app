package com.example.farmerbackend.service;

import com.example.farmerbackend.entity.CartItem;
import com.example.farmerbackend.entity.Product;
import com.example.farmerbackend.entity.User;
import com.example.farmerbackend.repository.CartItemRepository;
import com.example.farmerbackend.repository.ProductRepository;
import com.example.farmerbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public CartItem addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantityAvailable() < quantity) {
            throw new RuntimeException("Insufficient product quantity");
        }

        CartItem cartItem = cartRepository.findByUserUserIdAndProductProductId(userId, productId)
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        } else {
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > product.getQuantityAvailable()) {
                throw new RuntimeException("Insufficient product quantity");
            }
            cartItem.setQuantity(newQuantity);
        }

        return cartRepository.save(cartItem);
    }

    public List<CartItem> getCart(Long userId) {
        return cartRepository.findByUserUserId(userId);
    }

    public CartItem updateCart(Long cartItemId, int quantity) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        if (quantity > item.getProduct().getQuantityAvailable()) {
            throw new RuntimeException("Insufficient product quantity");
        }

        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    public void removeFromCart(Long cartItemId) {
        if (!cartRepository.existsById(cartItemId)) {
            throw new RuntimeException("Cart item not found");
        }
        cartRepository.deleteById(cartItemId);
    }

    public void clearCart(Long userId) {
        cartRepository.deleteByUserUserId(userId);
    }
}