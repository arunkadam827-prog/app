package com.example.farmerbackend.controller;

import com.example.farmerbackend.dto.OrderRequest;
import com.example.farmerbackend.entity.CustomerOrder;
import com.example.farmerbackend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

 private final OrderService orderService;

 public OrderController(OrderService orderService) {
  this.orderService = orderService;
 }

 @PostMapping
 public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
  try {
   CustomerOrder order = orderService.createOrder(
       request.getUserId(),
       request.getDeliveryAddress(),
       request.getPaymentMethod()
   );
   return ResponseEntity.ok(order);
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }

 @GetMapping
 public ResponseEntity<?> getAllOrders() {
  return ResponseEntity.ok(orderService.getAllOrders());
 }

 @GetMapping("/user/{userId}")
 public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
  return ResponseEntity.ok(orderService.getUserOrders(userId));
 }

 @GetMapping("/{orderId}")
 public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
  try {
   return ResponseEntity.ok(orderService.getOrder(orderId));
  } catch (Exception e) {
   return ResponseEntity.notFound().build();
  }
 }

 @PutMapping("/{orderId}/status")
 public ResponseEntity<?> updateStatus(@PathVariable Long orderId, @RequestParam String status) {
  try {
   return ResponseEntity.ok(orderService.updateStatus(orderId, status));
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }
}