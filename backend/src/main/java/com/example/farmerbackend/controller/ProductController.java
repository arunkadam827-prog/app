package com.example.farmerbackend.controller;

import com.example.farmerbackend.entity.Product;
import com.example.farmerbackend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

 private final ProductService productService;

 public ProductController(ProductService productService) {
  this.productService = productService;
 }

 @PostMapping({"/add", ""})
 public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> body) {
  try {
   Long farmerId = null;
   if (body.get("farmerId") != null) {
    farmerId = Long.valueOf(body.get("farmerId").toString());
   } else if (body.get("sellerId") != null) {
    farmerId = Long.valueOf(body.get("sellerId").toString());
   }
   if (farmerId == null) {
    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "farmerId is required"));
   }

   Product product = new Product();
   product.setProductName((String) body.get("productName"));
   if (body.get("price") != null) {
    product.setPrice(Double.parseDouble(body.get("price").toString()));
   }
   if (body.get("quantityAvailable") != null) {
    product.setQuantityAvailable(Integer.parseInt(body.get("quantityAvailable").toString()));
   }
   product.setDescription((String) body.get("description"));
   product.setCategory(body.get("category") != null ? (String) body.get("category") : "General");
   product.setImageUrl(body.get("imageUrl") != null ? (String) body.get("imageUrl") : "");

   Product saved = productService.addProduct(farmerId, product);
   return ResponseEntity.ok(saved);
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }

 @PostMapping("/farmer/{farmerId}/bulk")
 public ResponseEntity<?> addProducts(
         @PathVariable Long farmerId,
         @RequestBody List<Product> products) {

  try {
   List<Product> savedProducts = new ArrayList<>();

   for (Product product : products) {
    savedProducts.add(productService.addProduct(farmerId, product));
   }

   return ResponseEntity.ok(savedProducts);

  } catch (Exception e) {
   return ResponseEntity.badRequest()
           .body(Map.of("success", false, "message", e.getMessage()));
  }
 }


 @GetMapping
 public ResponseEntity<?> getAllProducts() {
  return ResponseEntity.ok(productService.getAllProducts());
 }

 @GetMapping("/{id}")
 public ResponseEntity<?> getProduct(@PathVariable Long id) {
  try {
   return ResponseEntity.ok(productService.getProduct(id));
  } catch (Exception e) {
   return ResponseEntity.notFound().build();
  }
 }

 @GetMapping("/farmer/{farmerId}")
 public ResponseEntity<?> getFarmerProducts(@PathVariable Long farmerId) {
  return ResponseEntity.ok(productService.getFarmerProducts(farmerId));
 }

 @GetMapping("/category/{category}")
 public ResponseEntity<?> getByCategory(@PathVariable String category) {
  return ResponseEntity.ok(productService.getByCategory(category));
 }

 @GetMapping("/search")
 public ResponseEntity<?> search(@RequestParam String keyword) {
  return ResponseEntity.ok(productService.search(keyword));
 }

 @PutMapping("/{productId}/farmer/{farmerId}")
 public ResponseEntity<?> updateProduct(@PathVariable Long productId, @PathVariable Long farmerId, @RequestBody Product product) {
  try {
   return ResponseEntity.ok(productService.updateProduct(productId, farmerId, product));
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }

 @DeleteMapping("/{productId}/farmer/{farmerId}")
 public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @PathVariable Long farmerId) {
  try {
   productService.deleteProduct(productId, farmerId);
   return ResponseEntity.ok(Map.of("success", true, "message", "Product deleted successfully"));
  } catch (Exception e) {
   return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
  }
 }
}