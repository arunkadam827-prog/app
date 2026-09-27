package com.example.farmer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.farmer.R;
import com.example.farmer.models.User;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerDashboardActivity extends AppCompatActivity {

 private static final String TAG = "FarmerDashboard";

 // Stat Values
 private TextView productsNumber, ordersNumber, revenueNumber, ratingNumber;
 private TextView welcomeText;

 // Action Buttons
 private Button addProductButton, viewProductsButton, viewOrdersButton, analyticsButton;

 private SessionManager sessionManager;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_farmer_dashboard);

  sessionManager = new SessionManager(this);

  // Professional Step: Initialize Toolbar for Logout/Profile access
  Toolbar toolbar = findViewById(R.id.toolbar);
  setSupportActionBar(toolbar);
  if (getSupportActionBar() != null) {
   getSupportActionBar().setTitle("Farmer Dashboard");
  }

  initViews();
  setupClickListeners();

  // Personalize UI
  welcomeText.setText("Welcome back, " + sessionManager.getUserName() + "!");
 }

 private void initViews() {
  welcomeText = findViewById(R.id.welcome_text);

  // Stat Values
  productsNumber = findViewById(R.id.products_number);
  ordersNumber = findViewById(R.id.orders_number);
  revenueNumber = findViewById(R.id.revenue_number);
  ratingNumber = findViewById(R.id.rating_number);

  // Action Buttons
  addProductButton = findViewById(R.id.add_product_button);
  viewProductsButton = findViewById(R.id.view_products_button);
  viewOrdersButton = findViewById(R.id.view_orders_button);
  findViewById(R.id.buy_products_button).setOnClickListener(v ->
          startActivity(new Intent(this, MainActivity.class)));
  findViewById(R.id.view_cart_button).setOnClickListener(v -> {
      Intent intent = new Intent(this, MainActivity.class);
      intent.putExtra("open_tab", "cart");
      startActivity(intent);
  });
 }

 private void setupClickListeners() {
  // Stats Cards Logic
  findViewById(R.id.products_count).setOnClickListener(v ->
          startActivity(new Intent(this, ProductListActivity.class)));

  findViewById(R.id.orders_count).setOnClickListener(v ->
          startActivity(new Intent(this, OrdersActivity.class)));

  // Action Buttons Logic
  addProductButton.setOnClickListener(v ->
          startActivity(new Intent(this, AddProductActivity.class)));

  viewProductsButton.setOnClickListener(v ->
          startActivity(new Intent(this, ProductListActivity.class)));

  viewOrdersButton.setOnClickListener(v ->
          startActivity(new Intent(this, OrdersActivity.class)));
 }

 private void fetchDashboardStats() {
  Long farmerId = sessionManager.getUserId();

  // Example API Call to get stats (You'll need to add this endpoint to your ApiService)
  RetrofitClient.getApiService().getFarmerStats(farmerId).enqueue(new Callback<Map<String, Object>>() {
   @Override
   public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
    if (response.isSuccessful() && response.body() != null) {
     Map<String, Object> stats = response.body();

     productsNumber.setText(String.valueOf(stats.get("productCount")));
     ordersNumber.setText(String.valueOf(stats.get("orderCount")));
     revenueNumber.setText("₹ " + stats.get("totalRevenue"));
     ratingNumber.setText(String.valueOf(stats.get("avgRating")));
    }
   }

   @Override
   public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
    Log.e(TAG, "Failed to fetch stats: " + t.getMessage());
   }
  });
 }

 // Professional Addition: Logout Menu
// Inside FarmerDashboardActivity.java

 // 1. Update this method to inflate the correct menu file name
 @Override
 public boolean onCreateOptionsMenu(Menu menu) {
  getMenuInflater().inflate(R.menu.drawer_menu, menu); // Changed from dashboard_menu to drawer_menu
  return true;
 }

 // 2. Update this method to check for the correct menu item ID
 @Override
 public boolean onOptionsItemSelected(@NonNull MenuItem item) {
  if (item.getItemId() == R.id.nav_logout) { // Changed from action_logout to nav_logout
   handleLogout();
   return true;
  }
  return super.onOptionsItemSelected(item);
 }

 private void handleLogout() {
  sessionManager.logout();
  Intent intent = new Intent(this, LoginActivity.class);
  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
  startActivity(intent);
  finish();
 }

 @Override
 protected void onResume() {
  super.onResume();
  fetchDashboardStats();
 }
}