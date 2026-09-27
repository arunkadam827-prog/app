package com.example.farmerbackend.controller;

import com.example.farmerbackend.dto.GoogleLoginRequest;
import com.example.farmerbackend.dto.LoginRequest;
import com.example.farmerbackend.dto.LoginResponse;  // ✅ Import
import com.example.farmerbackend.dto.RegisterRequest;
import com.example.farmerbackend.entity.User;
import com.example.farmerbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            // ✅ FIXED: Return LoginResponse instead of Map
            return ResponseEntity.ok(
                    new LoginResponse(true, "Login successful", user)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, e.getMessage(), null));
        }
    }

    // ✅ Google Sign-In (Credential Manager): verify the ID token, then
    // create or log in the account by verified email.
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            User user = userService.googleLogin(request);
            return ResponseEntity.ok(
                    new LoginResponse(true, "Google sign-in successful", user)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUser(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ NEW: Add Update Endpoint
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}