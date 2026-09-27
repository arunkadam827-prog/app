package com.example.farmer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class OrdersFragment extends Fragment {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private ApiService apiService; // Switched from FarmerDatabase
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    private View emptyView;
    private TextView tvAllCount, tvPendingCount, tvDeliveredCount, tvCancelledCount;
    private final List<Order> allOrders = new ArrayList<>();
    private String selectedFilter = "ALL";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        sessionManager = new SessionManager(requireContext());
        ordersRecyclerView = view.findViewById(R.id.orders_recycler_view);
        progressBar = view.findViewById(R.id.orders_progress_bar);
        emptyView = view.findViewById(R.id.orders_empty_view);

        tvAllCount = view.findViewById(R.id.all_orders_count);
        tvPendingCount = view.findViewById(R.id.pending_orders_count);
        tvDeliveredCount = view.findViewById(R.id.delivered_orders_count);
        tvCancelledCount = view.findViewById(R.id.cancelled_orders_count);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Setup Adapter
        orderAdapter = new OrderAdapter(new ArrayList<>());
        ordersRecyclerView.setAdapter(orderAdapter);

        // 3. Initialize Retrofit Service
        apiService = RetrofitClient.getApiService();

        // 4. Setup filter click listeners
        View cardAll = view.findViewById(R.id.card_orders_all);
        if (cardAll != null) cardAll.setOnClickListener(v -> filterOrders("ALL"));

        View cardPending = view.findViewById(R.id.card_orders_pending);
        if (cardPending != null) cardPending.setOnClickListener(v -> filterOrders("PENDING"));

        View cardDelivered = view.findViewById(R.id.card_orders_delivered);
        if (cardDelivered != null) cardDelivered.setOnClickListener(v -> filterOrders("DELIVERED"));

        View cardCancelled = view.findViewById(R.id.card_orders_cancelled);
        if (cardCancelled != null) cardCancelled.setOnClickListener(v -> filterOrders("CANCELLED"));

        View viewAll = view.findViewById(R.id.view_all_orders);
        if (viewAll != null) viewAll.setOnClickListener(v -> filterOrders("ALL"));

        // 5. Fetch Data from PostgreSQL
        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        long userId = sessionManager.getUserId();

        apiService.getUserOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear();
                    allOrders.addAll(response.body());
                    updateCounters();
                    filterOrders(selectedFilter);
                } else {
                    Log.e("OrdersFragment", "Server Error: " + response.message());
                    showToast("Failed to load orders");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                Log.e("OrdersFragment", "Network Error: " + t.getMessage());
                showToast("Network error: check your connection");
            }
        });
    }

    private void updateCounters() {
        int all = allOrders.size();
        int pending = 0;
        int delivered = 0;
        int cancelled = 0;

        for (Order o : allOrders) {
            String status = o.getOrderStatus() != null ? o.getOrderStatus().toUpperCase() : "PENDING";
            if (status.contains("DELIVER")) {
                delivered++;
            } else if (status.contains("CANCEL")) {
                cancelled++;
            } else {
                pending++;
            }
        }

        if (tvAllCount != null) tvAllCount.setText(String.valueOf(all));
        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(pending));
        if (tvDeliveredCount != null) tvDeliveredCount.setText(String.valueOf(delivered));
        if (tvCancelledCount != null) tvCancelledCount.setText(String.valueOf(cancelled));
    }

    private void filterOrders(String filter) {
        selectedFilter = filter;
        List<Order> filtered = new ArrayList<>();

        for (Order o : allOrders) {
            String status = o.getOrderStatus() != null ? o.getOrderStatus().toUpperCase() : "PENDING";
            if ("ALL".equalsIgnoreCase(filter)) {
                filtered.add(o);
            } else if ("PENDING".equalsIgnoreCase(filter)) {
                if (!status.contains("DELIVER") && !status.contains("CANCEL")) {
                    filtered.add(o);
                }
            } else if ("DELIVERED".equalsIgnoreCase(filter)) {
                if (status.contains("DELIVER")) {
                    filtered.add(o);
                }
            } else if ("CANCELLED".equalsIgnoreCase(filter)) {
                if (status.contains("CANCEL")) {
                    filtered.add(o);
                }
            }
        }

        orderAdapter.updateList(filtered);

        if (emptyView != null) {
            emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (ordersRecyclerView != null) {
            ordersRecyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}