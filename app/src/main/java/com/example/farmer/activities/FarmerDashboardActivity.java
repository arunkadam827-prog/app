package com.example.farmer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.adapters.LeadAdapter;
import com.example.farmer.models.Lead;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerDashboardActivity extends AppCompatActivity {

        private static final String TAG = "FarmerDashboard";

        // Stat Values
        private TextView productsNumber, ordersNumber, revenueNumber, ratingNumber;
        private TextView welcomeText;

        // Buyer inquiries (leads)
        private TextView leadsNumber, leadsSummary;
        private List<Lead> cachedLeads = new ArrayList<>();

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

                // Buyer inquiries summary
                leadsNumber = findViewById(R.id.leads_number);
                leadsSummary = findViewById(R.id.leads_summary);

                // Action Buttons
                addProductButton = findViewById(R.id.add_product_button);
                viewProductsButton = findViewById(R.id.view_products_button);
                viewOrdersButton = findViewById(R.id.view_orders_button);
                findViewById(R.id.buy_products_button)
                                .setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
                findViewById(R.id.view_cart_button).setOnClickListener(v -> {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("open_tab", "cart");
                        startActivity(intent);
                });
        }

        private void setupClickListeners() {
                // Stats Cards Logic
                findViewById(R.id.products_count)
                                .setOnClickListener(v -> startActivity(new Intent(this, ProductListActivity.class)));

                findViewById(R.id.orders_count)
                                .setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));

                // Buyer inquiries -> open the leads bottom sheet
                findViewById(R.id.leads_count).setOnClickListener(v -> showLeadsSheet());

                // Action Buttons Logic
                addProductButton.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

                viewProductsButton.setOnClickListener(v -> startActivity(new Intent(this, ProductListActivity.class)));

                viewOrdersButton.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        }

        private void fetchDashboardStats() {
                Long farmerId = sessionManager.getUserId();

                // Example API Call to get stats (You'll need to add this endpoint to your
                // ApiService)
                RetrofitClient.getApiService().getFarmerStats(farmerId).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(@NonNull Call<Map<String, Object>> call,
                                        @NonNull Response<Map<String, Object>> response) {
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

        // =========================================================
        // Buyer inquiries (leads) — fetch, display & respond
        // =========================================================

        /** Loads the leads that buyers created for this farmer's products. */
        private void fetchLeads() {
                Long farmerId = sessionManager.getUserId();
                if (farmerId == null || farmerId <= 0) {
                        return;
                }

                RetrofitClient.getApiService().getFarmerLeads(farmerId)
                                .enqueue(new Callback<List<Lead>>() {
                                        @Override
                                        public void onResponse(@NonNull Call<List<Lead>> call,
                                                        @NonNull Response<List<Lead>> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                        cachedLeads = response.body();
                                                } else {
                                                        cachedLeads = new ArrayList<>();
                                                }
                                                updateLeadsSummary();
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<List<Lead>> call, @NonNull Throwable t) {
                                                Log.e(TAG, "Failed to fetch leads: " + t.getMessage());
                                        }
                                });
        }

        /** Reflects the current lead count on the dashboard entry card. */
        private void updateLeadsSummary() {
                int count = cachedLeads != null ? cachedLeads.size() : 0;

                if (leadsNumber != null) {
                        leadsNumber.setText(String.valueOf(count));
                }
                if (leadsSummary != null && count > 0) {
                        leadsSummary.setText(getString(R.string.leads_subtitle_count, count));
                }
        }

        /** Opens the bottom sheet listing every buyer inquiry. */
        private void showLeadsSheet() {
                BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.ThemeOverlay_Farmer_BottomSheet);
                View sheetView = getLayoutInflater().inflate(R.layout.sheet_farmer_leads, null, false);
                dialog.setContentView(sheetView);

                View emptyState = sheetView.findViewById(R.id.sheet_leads_empty);
                TextView subtitle = sheetView.findViewById(R.id.sheet_leads_subtitle);
                RecyclerView recycler = sheetView.findViewById(R.id.sheet_leads_recycler);

                sheetView.findViewById(R.id.sheet_leads_close).setOnClickListener(v -> dialog.dismiss());

                LeadAdapter adapter = new LeadAdapter(this, new LeadAdapter.OnLeadActionListener() {
                        @Override
                        public void onCallClick(Lead lead) {
                                callBuyer(lead, dialog);
                        }

                        @Override
                        public void onChatClick(Lead lead) {
                                openLeadWhatsApp(lead, dialog);
                        }

                        @Override
                        public void onInAppChatClick(Lead lead) {
                                openLeadInAppChat(lead, dialog);
                        }
                });

                recycler.setLayoutManager(new LinearLayoutManager(this));
                recycler.setAdapter(adapter);

                if (cachedLeads == null || cachedLeads.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recycler.setVisibility(View.GONE);
                } else {
                        emptyState.setVisibility(View.GONE);
                        recycler.setVisibility(View.VISIBLE);
                        adapter.updateList(cachedLeads);
                        subtitle.setText(getString(R.string.leads_subtitle_count, cachedLeads.size()));
                }

                dialog.show();

                // Always refresh in the background so the sheet is current.
                fetchLeads();
        }

        /** Dials the buyer's number directly from the inquiry. */
        private void callBuyer(Lead lead, BottomSheetDialog dialog) {
                String phone = normalizePhone(lead != null ? lead.getBuyerPhone() : null);
                if (phone.isEmpty()) {
                        Toast.makeText(this, R.string.lead_no_contact, Toast.LENGTH_SHORT).show();
                        return;
                }

                try {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                } catch (Exception e) {
                        Toast.makeText(this, R.string.lead_call_failed, Toast.LENGTH_SHORT).show();
                }

                markLeadContacted(lead, dialog);
        }

        /**
         * Opens an end-to-end (E2E) WhatsApp chat with the buyer. The conversation
         * is encrypted inside the WhatsApp client; the app only hands off the number
         * and a pre-filled opening message.
         */
        private void openLeadWhatsApp(Lead lead, BottomSheetDialog dialog) {
                String phone = normalizePhone(lead != null ? lead.getBuyerPhone() : null);
                if (phone.isEmpty()) {
                        Toast.makeText(this, R.string.lead_no_contact, Toast.LENGTH_SHORT).show();
                        return;
                }

                String buyerName = !TextUtils.isEmpty(lead.getBuyerName())
                                ? lead.getBuyerName()
                                : getString(R.string.lead_interested_buyer);
                String productName = !TextUtils.isEmpty(lead.getProductName())
                                ? lead.getProductName()
                                : getString(R.string.lead_your_produce);
                String message = getString(R.string.lead_chat_greeting, buyerName, productName);

                try {
                        String url = "https://api.whatsapp.com/send?phone=" + phone
                                        + "&text=" + URLEncoder.encode(message, "UTF-8");
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                        Toast.makeText(this, R.string.lead_share_failed, Toast.LENGTH_SHORT).show();
                }

                markLeadContacted(lead, dialog);
        }

        /** Opens the in-app chat screen for a threaded conversation with the buyer. */
        private void openLeadInAppChat(Lead lead, BottomSheetDialog dialog) {
                if (lead == null || lead.getBuyerId() == null) {
                        Toast.makeText(this, R.string.lead_no_contact, Toast.LENGTH_SHORT).show();
                        return;
                }
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_PARTNER_ID, (long) lead.getBuyerId());
                intent.putExtra(ChatActivity.EXTRA_PARTNER_NAME, lead.getBuyerName());
                intent.putExtra(ChatActivity.EXTRA_PRODUCT_NAME, lead.getProductName());
                startActivity(intent);
                markLeadContacted(lead, dialog);
        }

        /**
         * Normalises a stored phone number into an international WhatsApp/tel format.
         */
        private String normalizePhone(String raw) {
                if (raw == null) {
                        return "";
                }
                String digits = raw.replaceAll("[^0-9]", "");
                if (digits.length() == 10) {
                        digits = "91" + digits; // Default to India country code for local numbers.
                }
                return digits;
        }

        /**
         * Marks a lead as CONTACTED once the farmer reaches out, then refreshes the
         * list.
         */
        private void markLeadContacted(Lead lead, BottomSheetDialog dialog) {
                if (lead == null || lead.getId() == null) {
                        return;
                }
                if ("CONTACTED".equalsIgnoreCase(lead.getStatus())) {
                        return;
                }

                lead.setStatus("CONTACTED");

                RetrofitClient.getApiService().updateLeadStatus(lead.getId(), "CONTACTED")
                                .enqueue(new Callback<Lead>() {
                                        @Override
                                        public void onResponse(@NonNull Call<Lead> call,
                                                        @NonNull Response<Lead> response) {
                                                Log.d(TAG, "Lead " + lead.getId() + " marked as CONTACTED");
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<Lead> call, @NonNull Throwable t) {
                                                Log.e(TAG, "Could not update lead status: " + t.getMessage());
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
                fetchLeads();
        }
}
