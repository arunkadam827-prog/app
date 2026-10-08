package com.example.farmer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.activities.ProductDetailActivity;
import com.example.farmer.activities.ProductListActivity;
import com.example.farmer.adapters.ProductAdapter;
import com.example.farmer.models.Product;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Buyer home / discovery screen.
 *
 * This fragment owns the "proper" marketplace filtering so the duplicated
 * {@code ProductsFragment} could be removed. Category chips, the search field
 * and the advanced bottom sheet all funnel through {@link #applyFilters()},
 * which updates both the Featured and Best sellers rails.
 *
 * "View all" and the advanced screen open {@link ProductListActivity}, the
 * single full-list surface.
 */
public class HomeFragment extends Fragment {

    private ProductAdapter featuredAdapter;
    private ProductAdapter bestSellersAdapter;
    private ApiService apiService;

    private ProgressBar progressBar;
    private View emptyView;
    private View featuredSection;
    private View bestSellersSection;

    // Master list for local filtering (search / categories / price / sort)
    private List<Product> allProducts = new ArrayList<>();

    private String activeCategory = null;
    private String activeSearch = "";
    private boolean farmDirectOnly = false;
    private Double minPrice = null;
    private Double maxPrice = null;
    private SortMode sortMode = SortMode.RELEVANCE;

    private enum SortMode {
        RELEVANCE, PRICE_LOW_HIGH, PRICE_HIGH_LOW
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.orders_progress_bar);
        emptyView = view.findViewById(R.id.home_empty_view);
        featuredSection = view.findViewById(R.id.featured_section);
        bestSellersSection = view.findViewById(R.id.best_sellers_section);
        EditText searchBar = view.findViewById(R.id.search_edit_text);

        // Feature rail
        RecyclerView rvFeatured = view.findViewById(R.id.featured_products_recycler_view);
        rvFeatured.setLayoutManager(new GridLayoutManager(getContext(), 2));
        featuredAdapter = new ProductAdapter(getContext(), this::addToCart);
        featuredAdapter.setOnProductClickListener(this::openProductDetail);
        rvFeatured.setAdapter(featuredAdapter);

        // Best sellers rail
        RecyclerView rvBestSellers = view.findViewById(R.id.best_sellers_recycler_view);
        rvBestSellers.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bestSellersAdapter = new ProductAdapter(getContext(), this::addToCart);
        bestSellersAdapter.setOnProductClickListener(this::openProductDetail);
        rvBestSellers.setAdapter(bestSellersAdapter);

        setupClickListeners(view);
        setupCategoryChips(view);

        // Search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activeSearch = s.toString();
                applyFilters();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        apiService = RetrofitClient.getApiService();
        fetchProducts();
    }

    private void setupClickListeners(View view) {
        // "View all" -> full marketplace activity (nothing duplicated here).
        View viewAll = view.findViewById(R.id.view_all_button);
        if (viewAll != null) {
            viewAll.setOnClickListener(v -> openMarketplace(null));
        }
        View viewAllSellers = view.findViewById(R.id.view_all_sellers_button);
        if (viewAllSellers != null) {
            viewAllSellers.setOnClickListener(v -> openMarketplace(null));
        }

        // Category cards -> apply the matching category filter in-place.
        View veg = view.findViewById(R.id.category_vegetables);
        if (veg != null) {
            veg.setOnClickListener(v -> selectCategory("Vegetables"));
        }
        View fruits = view.findViewById(R.id.category_fruits);
        if (fruits != null) {
            fruits.setOnClickListener(v -> selectCategory("Fruits"));
        }
        View grains = view.findViewById(R.id.category_grains);
        if (grains != null) {
            grains.setOnClickListener(v -> selectCategory("Grains"));
        }
        View dairy = view.findViewById(R.id.category_dairy);
        if (dairy != null) {
            dairy.setOnClickListener(v -> selectCategory("Dairy"));
        }

        // Advanced filters sheet
        View openFilters = view.findViewById(R.id.home_open_filters);
        if (openFilters != null) {
            openFilters.setOnClickListener(v -> showFilterSheet());
        }

        // "Show everything" shortcut from the empty state.
        View emptyAction = view.findViewById(R.id.home_empty_action);
        if (emptyAction != null) {
            emptyAction.setOnClickListener(v -> openMarketplace(null));
        }
    }

    private void setupCategoryChips(View view) {
        View.OnClickListener chipListener = v -> {
            int id = v.getId();
            if (id == R.id.home_filter_all) {
                activeCategory = null;
                farmDirectOnly = false;
            } else if (id == R.id.home_filter_farm_direct) {
                activeCategory = null;
                farmDirectOnly = true;
            } else if (id == R.id.home_filter_vegetables) {
                activeCategory = "Vegetables";
                farmDirectOnly = false;
            } else if (id == R.id.home_filter_fruits) {
                activeCategory = "Fruits";
                farmDirectOnly = false;
            } else if (id == R.id.home_filter_grains) {
                activeCategory = "Grains";
                farmDirectOnly = false;
            } else if (id == R.id.home_filter_dairy) {
                activeCategory = "Dairy";
                farmDirectOnly = false;
            } else if (id == R.id.home_filter_others) {
                activeCategory = "Others";
                farmDirectOnly = false;
            }
            applyFilters();
        };
        int[] chips = { R.id.home_filter_all, R.id.home_filter_farm_direct, R.id.home_filter_vegetables,
                R.id.home_filter_fruits, R.id.home_filter_grains,
                R.id.home_filter_dairy, R.id.home_filter_others };
        for (int id : chips) {
            View chip = view.findViewById(id);
            if (chip != null) {
                chip.setOnClickListener(chipListener);
            }
        }
    }

    private void selectCategory(String category) {
        activeCategory = category;
        farmDirectOnly = false;
        syncChipsToCategory();
        applyFilters();
    }

    private void syncChipsToCategory() {
        if (getView() == null) {
            return;
        }
        int id = R.id.home_filter_all;
        if (farmDirectOnly)
            id = R.id.home_filter_farm_direct;
        else if ("Vegetables".equals(activeCategory))
            id = R.id.home_filter_vegetables;
        else if ("Fruits".equals(activeCategory))
            id = R.id.home_filter_fruits;
        else if ("Grains".equals(activeCategory))
            id = R.id.home_filter_grains;
        else if ("Dairy".equals(activeCategory))
            id = R.id.home_filter_dairy;
        else if ("Others".equals(activeCategory))
            id = R.id.home_filter_others;

        Chip chip = getView().findViewById(id);
        if (chip != null) {
            chip.setChecked(true);
        }
    }

    private void fetchProducts() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    applyFilters();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                showToast("Connection Error: Server is unreachable.");
            }
        });
    }

    /**
     * Single source of truth for the Home rails. Applies the active search
     * term, category, price window and sort order, then refreshes the UI.
     */
    private void applyFilters() {
        List<Product> filtered = new ArrayList<>();
        String query = activeSearch == null ? "" : activeSearch.trim().toLowerCase(Locale.US);

        for (Product p : allProducts) {
            String name = p.getProductName() != null ? p.getProductName().toLowerCase(Locale.US) : "";
            String cat = p.getCategory() != null ? p.getCategory() : "";

            boolean matchesSearch = query.isEmpty() || name.contains(query);
            boolean matchesCategory = activeCategory == null
                    || cat.equalsIgnoreCase(activeCategory)
                    || ("Others".equals(activeCategory) && !isKnownCategory(cat));

            boolean matchesPrice = true;
            double price = p.getPrice();
            if (minPrice != null && price < minPrice)
                matchesPrice = false;
            if (maxPrice != null && price > maxPrice)
                matchesPrice = false;

            boolean matchesFarm = !farmDirectOnly || p.isDirectFromFarm();

            if (matchesSearch && matchesCategory && matchesPrice && matchesFarm) {
                filtered.add(p);
            }
        }

        if (sortMode == SortMode.PRICE_LOW_HIGH) {
            Collections.sort(filtered, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        } else if (sortMode == SortMode.PRICE_HIGH_LOW) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        }

        featuredAdapter.updateList(new ArrayList<>(filtered));
        bestSellersAdapter.updateList(new ArrayList<>(filtered));
        updateEmptyState(filtered.size());
    }

    private boolean isKnownCategory(String category) {
        if (category == null)
            return false;
        String c = category.toLowerCase(Locale.US);
        return c.contains("vegetable") || c.contains("fruit")
                || c.contains("grain") || c.contains("dairy");
    }

    private void updateEmptyState(int count) {
        boolean empty = count == 0;
        if (emptyView != null) {
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (featuredSection != null) {
            featuredSection.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (bestSellersSection != null) {
            bestSellersSection.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    // ==================== FILTER SHEET ====================
    private void showFilterSheet() {
        if (!isAdded()) {
            return;
        }
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext(), R.style.ThemeOverlay_Farmer_BottomSheet);
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_filters, null, false);
        sheet.setContentView(content);

        EditText minInput = content.findViewById(R.id.sheet_min_price);
        EditText maxInput = content.findViewById(R.id.sheet_max_price);
        RadioButton sortLow = content.findViewById(R.id.sheet_sort_price_low);
        RadioButton sortHigh = content.findViewById(R.id.sheet_sort_price_high);
        RadioButton sortRel = content.findViewById(R.id.sheet_sort_relevance);

        // Pre-fill sourcing state
        Chip sourceFarmChip = content.findViewById(R.id.sheet_source_farm);
        Chip sourceAllChip = content.findViewById(R.id.sheet_source_all);
        if (farmDirectOnly && sourceFarmChip != null) {
            sourceFarmChip.setChecked(true);
        } else if (sourceAllChip != null) {
            sourceAllChip.setChecked(true);
        }

        // Pre-fill current state
        int categoryChipId = R.id.sheet_cat_all;
        if ("Vegetables".equals(activeCategory))
            categoryChipId = R.id.sheet_cat_vegetables;
        else if ("Fruits".equals(activeCategory))
            categoryChipId = R.id.sheet_cat_fruits;
        else if ("Grains".equals(activeCategory))
            categoryChipId = R.id.sheet_cat_grains;
        else if ("Others".equals(activeCategory))
            categoryChipId = R.id.sheet_cat_others;
        Chip currentChip = content.findViewById(categoryChipId);
        if (currentChip != null) {
            currentChip.setChecked(true);
        }

        if (minPrice != null)
            minInput.setText(String.valueOf(minPrice));
        if (maxPrice != null)
            maxInput.setText(String.valueOf(maxPrice));

        if (sortMode == SortMode.PRICE_LOW_HIGH)
            sortLow.setChecked(true);
        else if (sortMode == SortMode.PRICE_HIGH_LOW)
            sortHigh.setChecked(true);
        else
            sortRel.setChecked(true);

        content.findViewById(R.id.sheet_close).setOnClickListener(v -> sheet.dismiss());

        content.findViewById(R.id.sheet_reset).setOnClickListener(v -> {
            activeCategory = null;
            farmDirectOnly = false;
            minPrice = null;
            maxPrice = null;
            sortMode = SortMode.RELEVANCE;
            syncChipsToCategory();
            applyFilters();
            sheet.dismiss();
        });

        content.findViewById(R.id.sheet_apply).setOnClickListener(v -> {
            Chip farmChip = content.findViewById(R.id.sheet_source_farm);
            farmDirectOnly = farmChip != null && farmChip.isChecked();

            if (((Chip) content.findViewById(R.id.sheet_cat_vegetables)).isChecked())
                activeCategory = "Vegetables";
            else if (((Chip) content.findViewById(R.id.sheet_cat_fruits)).isChecked())
                activeCategory = "Fruits";
            else if (((Chip) content.findViewById(R.id.sheet_cat_grains)).isChecked())
                activeCategory = "Grains";
            else if (((Chip) content.findViewById(R.id.sheet_cat_others)).isChecked())
                activeCategory = "Others";
            else
                activeCategory = null;

            minPrice = parseDouble(minInput.getText().toString());
            maxPrice = parseDouble(maxInput.getText().toString());

            if (sortLow.isChecked())
                sortMode = SortMode.PRICE_LOW_HIGH;
            else if (sortHigh.isChecked())
                sortMode = SortMode.PRICE_HIGH_LOW;
            else
                sortMode = SortMode.RELEVANCE;

            syncChipsToCategory();
            applyFilters();
            sheet.dismiss();
        });

        sheet.show();
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleApiError(Response<List<Product>> response) {
        String error = "Error " + response.code();
        try {
            if (response.errorBody() != null)
                Log.e("API", response.errorBody().string());
        } catch (IOException ignored) {
        }
        showToast(error + ": Failed to load marketplace.");
    }

    private void openMarketplace(String category) {
        if (!isAdded()) {
            return;
        }
        Intent intent = new Intent(requireContext(), ProductListActivity.class);
        intent.putExtra(ProductListActivity.EXTRA_CATEGORY, category);
        startActivity(intent);
    }

    private void addToCart(Product product) {
        if (!isAdded())
            return;
        SessionManager sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            showToast("Please log in to add items to cart");
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("productId", (long) product.getProductId());
        body.put("quantity", 1);

        apiService.addToCart(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast(product.getProductName() + " added to cart");
                } else {
                    showToast("Failed to add to cart");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void openProductDetail(Product product) {
        if (!isAdded())
            return;
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
        startActivity(intent);
    }

    private void showToast(String message) {
        if (isAdded())
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
