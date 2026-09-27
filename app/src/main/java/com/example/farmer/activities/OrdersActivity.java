package com.example.farmer.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.adapters.OrderAdapter;
import com.example.farmer.models.Order;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dedicated "Orders" screen opened from the checkout success dialog, the farmer
 * dashboard and the profile screen.
 *
 * Previously this activity only inflated the layout and never loaded any data,
 * which is why placed orders were never shown. It now fetches the logged-in
 * user's orders from the backend and renders them in the layout's
 * {@code content_container} placeholder.
 */
public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        // activity_orders.xml exposes a FrameLayout ("content_container") for
        // dynamic content and has no RecyclerView of its own, so build one here.
        orderAdapter = new OrderAdapter(new ArrayList<>());
        ordersRecyclerView = new RecyclerView(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(orderAdapter);
        ordersRecyclerView.setClipToPadding(false);

        ViewGroup container = findViewById(R.id.content_container);
        if (container != null) {
            container.addView(ordersRecyclerView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh so a newly placed order shows up immediately.
        loadOrders();
    }

    private void loadOrders() {
        long userId = sessionManager.getUserId();

        apiService.getUserOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderAdapter.updateList(response.body());
                    if (response.body().isEmpty()) {
                        Toast.makeText(OrdersActivity.this, "No orders yet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Server error: " + response.message());
                    Toast.makeText(OrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(OrdersActivity.this, "Network error: check your connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
