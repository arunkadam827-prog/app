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

import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.example.farmer.models.Lead;
import com.google.android.material.card.MaterialCardView;
import java.net.URLEncoder;
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
                    .fallback(R.drawable.ic_home)
                    .centerCrop()
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

        // Farmer Profile & Direct Sourcing Contact Card
        container.addView(buildFarmerContactCard());

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

    private View buildFarmerContactCard() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(16);
        cardParams.bottomMargin = dp(8);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.parseColor("#F4F9F4"));
        card.setStrokeColor(Color.parseColor("#C8E6C9"));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(12));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        // Header: Badge + Verified
        TextView header = new TextView(this);
        header.setText("🌱 DIRECT FROM FARM • VERIFIED PRODUCER");
        header.setTextSize(11);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(Color.parseColor("#2E7D32"));
        layout.addView(header);

        // Farmer Name
        TextView name = new TextView(this);
        name.setText("👨‍🌾 " + product.getFarmerName());
        name.setTextSize(16);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(Color.parseColor("#1C1C1E"));
        name.setPadding(0, dp(4), 0, 0);
        layout.addView(name);

        // Location & Sourcing
        TextView location = new TextView(this);
        location.setText("📍 Farm Location: " + product.getFarmerCity());
        location.setTextSize(13);
        location.setTextColor(Color.parseColor("#555555"));
        location.setPadding(0, dp(2), 0, dp(12));
        layout.addView(location);

        // Direct Action Buttons Row (Call + WhatsApp + In-App Chat)
        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setWeightSum(3f);

        // Button 1: Call Farmer
        MaterialButton btnCall = new MaterialButton(this);
        btnCall.setText("📞 Call");
        btnCall.setTextColor(Color.parseColor("#1B5E20"));
        btnCall.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
        LinearLayout.LayoutParams callParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        callParams.setMargins(0, 0, dp(4), 0);
        btnCall.setLayoutParams(callParams);
        btnCall.setOnClickListener(v -> {
            String phone = product.getFarmerPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        });
        actionsRow.addView(btnCall);

        // Button 2: WhatsApp Chat (E2E)
        MaterialButton btnWhatsApp = new MaterialButton(this);
        btnWhatsApp.setText("💬 WhatsApp");
        btnWhatsApp.setTextColor(Color.WHITE);
        btnWhatsApp.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#25D366")));
        LinearLayout.LayoutParams waParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        waParams.setMargins(dp(4), 0, dp(4), 0);
        btnWhatsApp.setLayoutParams(waParams);
        btnWhatsApp.setOnClickListener(v -> openWhatsAppChat());
        actionsRow.addView(btnWhatsApp);

        // Button 3: In-App Chat
        MaterialButton btnChat = new MaterialButton(this);
        btnChat.setText(R.string.chat_btn_chat);
        btnChat.setTextColor(Color.parseColor("#1976D2"));
        btnChat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
        LinearLayout.LayoutParams chatParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        chatParams.setMargins(dp(4), 0, 0, 0);
        btnChat.setLayoutParams(chatParams);
        btnChat.setOnClickListener(v -> openInAppChat());
        actionsRow.addView(btnChat);

        layout.addView(actionsRow);

        // Button 3: Send Direct Inquiry / Negotiate Lead
        MaterialButton btnInquiry = new MaterialButton(this);
        btnInquiry.setText("📩 Send Bulk Inquiry / Negotiate");
        btnInquiry.setTextColor(Color.parseColor("#1976D2"));
        btnInquiry.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
        LinearLayout.LayoutParams inqParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        inqParams.topMargin = dp(8);
        btnInquiry.setLayoutParams(inqParams);
        btnInquiry.setOnClickListener(v -> showInquiryDialog());
        layout.addView(btnInquiry);

        card.addView(layout);
        return card;
    }

    private void openInAppChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_PARTNER_ID, (long) product.getFarmerId());
        intent.putExtra(ChatActivity.EXTRA_PARTNER_NAME, product.getFarmerName());
        intent.putExtra(ChatActivity.EXTRA_PRODUCT_NAME, product.getProductName());
        startActivity(intent);
    }

    private void openWhatsAppChat() {
        String phone = product.getFarmerPhone();
        if (phone != null) {
            phone = phone.replaceAll("[^0-9]", "");
            if (phone.length() == 10) {
                phone = "91" + phone;
            }
        } else {
            phone = "919356601104";
        }
        String msg = "Hello " + product.getFarmerName() + ", I saw your product '"
                + product.getProductName() + "' (₹" + product.getPrice()
                + ") on Kisan Connect. I would like to discuss buying / negotiation.";
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(msg, "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showInquiryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Lead to " + product.getFarmerName());

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(10), dp(20), dp(10));

        EditText etQty = new EditText(this);
        etQty.setHint("Quantity required (e.g. 50 kg, 5 boxes)");
        etQty.setText(quantity + " units");
        form.addView(etQty);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Your Phone Number");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        etPhone.setText(sessionManager.getUserPhone());
        form.addView(etPhone);

        EditText etMsg = new EditText(this);
        etMsg.setHint("Message or offer price (optional)");
        form.addView(etMsg);

        builder.setView(form);
        builder.setPositiveButton("Send Lead", (dialog, which) -> {
            String reqQty = etQty.getText().toString().trim();
            String buyerPhone = etPhone.getText().toString().trim();
            String note = etMsg.getText().toString().trim();

            Lead lead = new Lead(
                    (long) product.getFarmerId(),
                    sessionManager.getUserId(),
                    sessionManager.getUserName(),
                    buyerPhone.isEmpty() ? sessionManager.getUserPhone() : buyerPhone,
                    (long) product.getProductId(),
                    product.getProductName(),
                    reqQty.isEmpty() ? "1 unit" : reqQty,
                    note);

            apiService.createLead(lead).enqueue(new Callback<Lead>() {
                @Override
                public void onResponse(Call<Lead> call, Response<Lead> response) {
                    Toast.makeText(ProductDetailActivity.this,
                            "Lead sent directly to " + product.getFarmerName() + "! They will contact you shortly.",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<Lead> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lead sent to farmer!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
