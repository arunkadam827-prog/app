package com.example.farmer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.activities.CheckoutActivity;
import com.example.farmer.adapters.CartAdapter;
import com.example.farmer.models.CartItem;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView tvTotalPrice;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnCheckout;

    private ApiService apiService;
    private SessionManager sessionManager;
    private List<CartItem> cartList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        apiService = RetrofitClient.getApiService();

        recyclerView = view.findViewById(R.id.cart_recycler_view);
        tvTotalPrice = view.findViewById(R.id.total_price_text);
        progressBar = view.findViewById(R.id.cart_progress_bar);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        btnCheckout = view.findViewById(R.id.checkout_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(cartList, this::removeItemFromServer);
        adapter.setOnCartQuantityChangeListener(this::updateItemQuantity);
        recyclerView.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> handleCheckout());

        loadCartFromServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartFromServer();
    }

    private void loadCartFromServer() {
        setLoadingState(true);
        long userId = sessionManager.getUserId();

        apiService.getCartByUserId(userId).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CartItem>> call, @NonNull Response<List<CartItem>> response) {
                setLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    cartList = response.body();
                } else {
                    cartList = new ArrayList<>();
                }
                updateUIState();
            }

            @Override
            public void onFailure(@NonNull Call<List<CartItem>> call, @NonNull Throwable t) {
                setLoadingState(false);
                cartList = new ArrayList<>();
                updateUIState();
            }
        });
    }

    private void updateUIState() {
        boolean isEmpty = (cartList == null || cartList.isEmpty());
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (adapter != null) {
            adapter.updateList(cartList);
        }
        calculateTotal();
        setLoadingState(false);
    }

    private void calculateTotal() {
        double total = 0.0;
        if (cartList != null) {
            for (CartItem item : cartList) {
                total += (item.getPrice() * item.getQuantity());
            }
        }
        if (tvTotalPrice != null) {
            tvTotalPrice.setText(String.format(Locale.US, "₹ %.2f", total));
        }
    }

    private void updateItemQuantity(CartItem item, int newQuantity) {
        if (item == null || item.getCartItemId() == null)
            return;
        if (newQuantity < 1)
            return;

        // Optimistic UI update so the cart feels instant, like Flipkart/Amazon.
        item.setQuantity(newQuantity);
        adapter.notifyDataSetChanged();
        calculateTotal();

        apiService.updateCart(item.getCartItemId(), newQuantity).enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(@NonNull Call<CartItem> call, @NonNull Response<CartItem> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Could not update quantity", Toast.LENGTH_SHORT).show();
                    loadCartFromServer();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartItem> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error updating quantity", Toast.LENGTH_SHORT).show();
                loadCartFromServer();
            }
        });
    }

    private void removeItemFromServer(CartItem item) {
        if (item == null || item.getCartItemId() == null)
            return;
        setLoadingState(true);

        apiService.removeFromCart(item.getCartItemId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    loadCartFromServer();
                } else {
                    setLoadingState(false);
                    Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setLoadingState(false);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckout() {
        if (cartList == null || cartList.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0.0;
        for (CartItem item : cartList) {
            total += (item.getPrice() * item.getQuantity());
        }

        Intent intent = new Intent(requireContext(), CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_TOTAL_AMOUNT, total);
        intent.putExtra(CheckoutActivity.EXTRA_ITEMS_COUNT, cartList.size());
        startActivity(intent);
    }

    private void setLoadingState(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnCheckout != null) {
            boolean hasItems = (cartList != null && !cartList.isEmpty());
            btnCheckout.setEnabled(!isLoading && hasItems);
        }
    }
}
