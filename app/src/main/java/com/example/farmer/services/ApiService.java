package com.example.farmer.services;

import com.example.farmer.models.*;
import com.example.farmer.dto.LoginResponse;
import com.example.farmer.dto.SocialLoginRequest;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/users/login")
    Call<LoginResponse> login(@Body Map<String, String> loginRequest);

    @POST("api/users/register")
    Call<User> registerUser(@Body User newUser);

    /**
     * Social sign-in: exchanges a verified Google ID token for an app session.
     * The backend validates the token and creates the account on first use.
     */
    @POST("api/users/google")
    Call<LoginResponse> googleSignIn(@Body SocialLoginRequest request);

    @GET("api/users/{id}")
    Call<User> getUserProfile(@Path("id") Long id);

    @PUT("api/users/update/{id}")
    Call<User> updateProfile(@Path("id") Long id, @Body User user);

    // ✅ FIX 1: Remove "/all". Standard Spring Boot list endpoints use the base
    // path.
    // If your backend specifically uses @GetMapping("/all"), change the backend to
    // @GetMapping("/list") to avoid conflict.
    @GET("api/products")
    Call<List<Product>> getAllProducts();

    // ✅ FIX 2: Change 'int' to 'Long' to match your backend's expected
    // 'java.lang.Long' type.
    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") Long id);

    @POST("api/products/add")
    Call<Product> addProduct(@Body Product product);

    @POST("api/cart")
    Call<Void> addToCart(@Body Map<String, Object> cartRequest);

    @GET("api/cart/{userId}")
    Call<List<CartItem>> getCartByUserId(@Path("userId") Long userId);

    @PUT("api/cart/{cartItemId}")
    Call<CartItem> updateCart(@Path("cartItemId") Long cartItemId, @Query("quantity") int quantity);

    @DELETE("api/cart/{cartItemId}")
    Call<Void> removeFromCart(@Path("cartItemId") Long cartItemId);

    @DELETE("api/cart/user/{userId}")
    Call<Void> clearCart(@Path("userId") Long userId);

    @GET("api/farmers/{id}/stats")
    Call<Map<String, Object>> getFarmerStats(@Path("id") Long farmerId);

    @GET("api/orders")
    Call<List<Order>> getAllOrders();

    @POST("api/orders")
    Call<Order> createOrder(@Body Map<String, Object> orderRequest);

    @GET("api/orders/user/{userId}")
    Call<List<Order>> getUserOrders(@Path("userId") Long userId);
}