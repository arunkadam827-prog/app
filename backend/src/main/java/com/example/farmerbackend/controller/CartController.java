package com.example.farmerbackend.controller;

import com.example.farmerbackend.dto.CartRequest;
import com.example.farmerbackend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

 private final CartService cartService;

 public CartController(CartService cartService) {
  this.cartService = cartService;
 }

 @PostMapping
 public ResponseEntity<?> addToCart(@RequestBody CartRequest request) {
  try {
   return ResponseEntity.ok(cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity()));
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }

 @GetMapping("/{userId}")
 public ResponseEntity<?> getCart(@PathVariable Long userId) {
  return ResponseEntity.ok(cartService.getCart(userId));
 }

 @PutMapping("/{cartItemId}")
 public ResponseEntity<?> updateCart(@PathVariable Long cartItemId, @RequestParam int quantity) {
  try {
   return ResponseEntity.ok(cartService.updateCart(cartItemId, quantity));
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }

 @DeleteMapping("/{cartItemId}")
 public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
  try {
   cartService.removeFromCart(cartItemId);
   return ResponseEntity.ok(Map.of("success", true, "message", "Removed from cart"));
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }

 @DeleteMapping("/user/{userId}")
 public ResponseEntity<?> clearCart(@PathVariable Long userId) {
  cartService.clearCart(userId);
  return ResponseEntity.ok(Map.of("success", true, "message", "Cart cleared"));
 }
}