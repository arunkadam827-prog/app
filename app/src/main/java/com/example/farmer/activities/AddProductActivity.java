package com.example.farmer.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;
import com.example.farmer.models.Product;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

 // 1. Declare the UI components at the class level
 private EditText productName;
 private EditText productPrice;
 private EditText productQuantity;
 private EditText productDescription;
 private Button saveProductButton;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_add_product);

  // 2. Initialize the UI components using the IDs from your XML layout
  productName = findViewById(R.id.product_name);
  productPrice = findViewById(R.id.product_price);
  productQuantity = findViewById(R.id.product_quantity);
  productDescription = findViewById(R.id.product_description);
  saveProductButton = findViewById(R.id.save_product_button);

  // 3. Set the click listener
  saveProductButton.setOnClickListener(v -> saveProduct());
 }

 private void saveProduct() {
  String name = productName.getText().toString().trim();
  String priceStr = productPrice.getText().toString().trim();
  String quantityStr = productQuantity.getText().toString().trim();
  String description = productDescription.getText().toString().trim();

  // Validation Logic
  if (name.isEmpty()) {
   productName.setError("Enter product name");
   productName.requestFocus();
   return;
  }
  if (priceStr.isEmpty()) {
   productPrice.setError("Enter product price");
   productPrice.requestFocus();
   return;
  }
  if (quantityStr.isEmpty()) {
   productQuantity.setError("Enter quantity");
   productQuantity.requestFocus();
   return;
  }
  if (description.isEmpty()) {
   productDescription.setError("Enter product description");
   productDescription.requestFocus();
   return;
  }

  // Prepare Data for Server
  double price = Double.parseDouble(priceStr);
  int quantity = Integer.parseInt(quantityStr);

  // Get the current Farmer's ID from SessionManager
  SessionManager sessionManager = new SessionManager(this);
  long farmerId = sessionManager.getUserId();

  // Create the Product object
  Product newProduct = new Product();
  newProduct.setProductName(name);
  newProduct.setPrice(price);
  newProduct.setQuantityAvailable(quantity);
  newProduct.setDescription(description);
  newProduct.setFarmerId((int) farmerId);

  // API Call via Retrofit
  ApiService apiService = RetrofitClient.getApiService();
  apiService.addProduct(newProduct).enqueue(new Callback<Product>() {
   @Override
   public void onResponse(Call<Product> call, Response<Product> response) {
    if (response.isSuccessful()) {
     Toast.makeText(AddProductActivity.this,
             "Product uploaded successfully", Toast.LENGTH_SHORT).show();
     finish();
    } else {
     Toast.makeText(AddProductActivity.this,
             "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
    }
   }

   @Override
   public void onFailure(Call<Product> call, Throwable t) {
    Toast.makeText(AddProductActivity.this,
            "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
   }
  });
 }
}