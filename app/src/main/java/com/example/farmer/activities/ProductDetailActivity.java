package com.example.farmer.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.farmer.R;
import com.example.farmer.models.Product;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Buyer-facing product detail screen (Flipkart/Amazon style).
 *
 * Shows the product image, price, stock and description, lets the buyer pick a
 * quantity, then Add to Cart or Buy Now. Opened from any product card.
 */
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";

    private ApiService apiService;
    private SessionManager sessionManager;
    private Product product;

    private int quantity = 1;
    private TextView tvQtyValue;
    private MaterialButton btnAddToCart, btnBuyNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        apiService = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);

        product = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buildUi();

        // Refresh from the server for the latest stock/price.
        loadProduct(product.getProductId());
    }

    private void loadProduct(int productId) {
        apiService.getProductById((long) productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    buildUi();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                // Keep the intent-provided product as a fallback.
            }
        });
    }

    private void buildUi() {
        LinearLayout container = findViewById(R.id.content_container);
        if (container == null)
            return;
        container.removeAllViews();
        container.setBackgroundColor(Color.WHITE);

        // Product image
        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(240)));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.parseColor("#F0F4F0"));
        String imageUrl = product.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Glide.with(this).load(R.drawable.ic_home).into(image);
        } else {
            Glide.with(this).load(imageUrl)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(image);
        }
        container.addView(image);

        // Category chip
        String category = product.getCategory() != null ? product.getCategory() : "General";
        TextView chip = new TextView(this);
        chip.setText("  " + category + "  ");
        chip.setTextColor(Color.parseColor("#2E7D32"));
        chip.setTextSize(12);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setBackgroundResource(R.drawable.bg_category_chip);
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipParams.topMargin = dp(16);
        chip.setLayoutParams(chipParams);
        container.addView(chip);

        // Product name
        TextView name = new TextView(this);
        name.setText(product.getProductName());
        name.setTextSize(22);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(Color.parseColor("#1C1C1E"));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameParams.topMargin = dp(10);
        name.setLayoutParams(nameParams);
        container.addView(name);

        // Seller
        if (!TextUtils.isEmpty(product.getFarmerName())) {
            TextView seller = new TextView(this);
            seller.setText("Sold by: " + product.getFarmerName());
            seller.setTextSize(13);
            seller.setTextColor(Color.parseColor("#5F6368"));
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sp.topMargin = dp(4);
            seller.setLayoutParams(sp);
            container.addView(seller);
        }

        // Price
        TextView price = new TextView(this);
        price.setText(String.format("₹ %.2f", product.getPrice()));
        price.setTextSize(26);
        price.setTypeface(null, Typeface.BOLD);
        price.setTextColor(Color.parseColor("#1B5E20"));
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        priceParams.topMargin = dp(12);
        price.setLayoutParams(priceParams);
        container.addView(price);

        // Stock status
        boolean inStock = product.getQuantityAvailable() > 0;
        TextView stock = new TextView(this);
        stock.setText(inStock
                ? ("In Stock (" + product.getQuantityAvailable() + " available)")
                : "Out of Stock");
        stock.setTextSize(13);
        stock.setTextColor(inStock ? Color.parseColor("#188038") : Color.parseColor("#D93025"));
        LinearLayout.LayoutParams stockParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stockParams.topMargin = dp(4);
        stock.setLayoutParams(stockParams);
        container.addView(stock);

        // Description
        if (!TextUtils.isEmpty(product.getDescription())) {
            TextView desc = new TextView(this);
            desc.setText(product.getDescription());
            desc.setTextSize(14);
            desc.setTextColor(Color.parseColor("#3C4043"));
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            descParams.topMargin = dp(14);
            desc.setLayoutParams(descParams);
            container.addView(desc);
        }

        // Quantity selector
        container.addView(buildQuantityRow(inStock));

        // Push buttons to the bottom
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        container.addView(spacer);

        // Add to Cart
        btnAddToCart = new MaterialButton(this);
        btnAddToCart.setText("🛒  Add to Cart");
        btnAddToCart.setTextColor(Color.WHITE);
        btnAddToCart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        btnAddToCart.setEnabled(inStock);
        LinearLayout.LayoutParams addP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        addP.topMargin = dp(12);
        btnAddToCart.setLayoutParams(addP);
        btnAddToCart.setOnClickListener(v -> addToCart(false));
        container.addView(btnAddToCart);

        // Buy Now
        btnBuyNow = new MaterialButton(this);
        btnBuyNow.setText("⚡  Buy Now");
        btnBuyNow.setTextColor(Color.WHITE);
        btnBuyNow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F9AB00")));
        btnBuyNow.setEnabled(inStock);
        LinearLayout.LayoutParams buyP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        buyP.topMargin = dp(8);
        buyP.bottomMargin = dp(16);
        btnBuyNow.setLayoutParams(buyP);
        btnBuyNow.setOnClickListener(v -> addToCart(true));
        container.addView(btnBuyNow);
    }

    private View buildQuantityRow(boolean inStock) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(18);
        row.setLayoutParams(rowParams);

        TextView label = new TextView(this);
        label.setText("Quantity");
        label.setTextSize(15);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(Color.parseColor("#202124"));
        row.addView(label);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        row.addView(spacer);

        MaterialButton minus = new MaterialButton(this);
        minus.setText("−");
        minus.setTextSize(18);
        minus.setPadding(dp(16), 0, dp(16), 0);
        minus.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(44)));
        minus.setEnabled(inStock);
        row.addView(minus);

        tvQtyValue = new TextView(this);
        tvQtyValue.setText(String.valueOf(quantity));
        tvQtyValue.setTextSize(18);
        tvQtyValue.setTypeface(null, Typeface.BOLD);
        tvQtyValue.setGravity(Gravity.CENTER);
        tvQtyValue.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(44)));
        row.addView(tvQtyValue);

        MaterialButton plus = new MaterialButton(this);
        plus.setText("+");
        plus.setTextSize(18);
        plus.setPadding(dp(16), 0, dp(16), 0);
        plus.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(44)));
        plus.setEnabled(inStock);
        row.addView(plus);

        minus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQtyValue.setText(String.valueOf(quantity));
            }
        });
        plus.setOnClickListener(v -> {
            if (quantity < product.getQuantityAvailable()) {
                quantity++;
                tvQtyValue.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this,
                        "Only " + product.getQuantityAvailable() + " available",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return row;
    }

    private void addToCart(boolean buyNow) {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("productId", (long) product.getProductId());
        body.put("quantity", quantity);

        apiService.addToCart(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    if (buyNow) {
                        Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
                        intent.putExtra("open_tab", "cart");
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ProductDetailActivity.this,
                                product.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(ProductDetailActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (btnAddToCart != null)
            btnAddToCart.setEnabled(!loading);
        if (btnBuyNow != null)
            btnBuyNow.setEnabled(!loading);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
