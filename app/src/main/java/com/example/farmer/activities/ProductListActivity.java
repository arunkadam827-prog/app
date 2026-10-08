package com.example.farmer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.adapters.ProductAdapter;
import com.example.farmer.models.Product;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

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
 * Full marketplace listing.
 *
 * This activity is the single "all products" surface for the app. It was
 * previously a duplicated fragment that overlapped with {@code HomeFragment};
 * the fragment was converted into this activity so the browsing experience
 * lives in exactly one place.
 *
 * Both farmers and buyers can open it and buy - a farmer logged in through the
 * dashboard reaches this screen through the regular buyer shell.
 */
public class ProductListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_QUERY = "extra_query";

    private ProductAdapter adapter;
    private ApiService apiService;

    private RecyclerView recyclerView;
    private TextView tvCount;
    private View emptyView;
    private ProgressBar progressBar;

    private final List<Product> allProducts = new ArrayList<>();
    private String activeCategory = null;
    private String activeSearch = "";
    private boolean farmDirectOnly = false;
    private boolean farmerProductsOnly = false; // filter by current farmer's own products

    // Advanced filter state
    private Double minPrice = null;
    private Double maxPrice = null;
    private SortMode sortMode = SortMode.RELEVANCE;

    private enum SortMode {
        RELEVANCE, PRICE_LOW_HIGH, PRICE_HIGH_LOW
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        apiService = RetrofitClient.getApiService();

        tvCount = findViewById(R.id.product_list_count);
        emptyView = findViewById(R.id.product_list_empty);
        progressBar = findViewById(R.id.product_list_progress);
        recyclerView = findViewById(R.id.product_list_recycler);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, this::addToCart);
        adapter.setOnProductClickListener(this::openProductDetail);
        adapter.setOnContactFarmerClickListener(this::openProductDetail); // "Contact Farmer" opens detail
        recyclerView.setAdapter(adapter);

        // Back navigation (replaced the fragment's implicit back handling).
        View back = findViewById(R.id.product_list_back);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        // Prefill filters coming from Home / dashboard shortcuts.
        activeCategory = getIntent().getStringExtra(EXTRA_CATEGORY);
        String query = getIntent().getStringExtra(EXTRA_QUERY);
        activeSearch = query != null ? query : "";

        // Search
        EditText search = findViewById(R.id.product_list_search);
        if (search != null) {
            if (!activeSearch.isEmpty()) {
                search.setText(activeSearch);
            }
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activeSearch = s.toString();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // Category chips
        View.OnClickListener chipListener = v -> {
            int id = v.getId();
            if (id == R.id.filter_all) {
                activeCategory = null;
                farmDirectOnly = false;
                farmerProductsOnly = false;
            } else if (id == R.id.filter_farm_direct) {
                activeCategory = null;
                farmDirectOnly = true;
                farmerProductsOnly = false;
            } else if (id == R.id.filter_farmer_products) {
                activeCategory = null;
                farmDirectOnly = false;
                farmerProductsOnly = true;
            } else if (id == R.id.filter_vegetables) {
                activeCategory = "Vegetables";
                farmDirectOnly = false;
                farmerProductsOnly = false;
            } else if (id == R.id.filter_fruits) {
                activeCategory = "Fruits";
                farmDirectOnly = false;
                farmerProductsOnly = false;
            } else if (id == R.id.filter_grains) {
                activeCategory = "Grains";
                farmDirectOnly = false;
                farmerProductsOnly = false;
            } else if (id == R.id.filter_others) {
                activeCategory = "Others";
                farmDirectOnly = false;
                farmerProductsOnly = false;
            }
            applyFilters();
        };
        int[] chips = { R.id.filter_all, R.id.filter_farm_direct, R.id.filter_farmer_products,
                R.id.filter_vegetables, R.id.filter_fruits,
                R.id.filter_grains, R.id.filter_others };
        for (int id : chips) {
            View chip = findViewById(id);
            if (chip != null) {
                chip.setOnClickListener(chipListener);
            }
        }
        preselectChipForCategory();

        // Advanced filters
        View filterButton = findViewById(R.id.product_list_filter);
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> showFilterSheet());
        }
        View sortButton = findViewById(R.id.product_list_sort);
        if (sortButton != null) {
            sortButton.setOnClickListener(v -> showFilterSheet());
        }

        fetchProducts();
    }

    private void preselectChipForCategory() {
        int preset = R.id.filter_all;
        if (farmerProductsOnly)
            preset = R.id.filter_farmer_products;
        else if (farmDirectOnly)
            preset = R.id.filter_farm_direct;
        else if ("Vegetables".equalsIgnoreCase(activeCategory))
            preset = R.id.filter_vegetables;
        else if ("Fruits".equalsIgnoreCase(activeCategory))
            preset = R.id.filter_fruits;
        else if ("Grains".equalsIgnoreCase(activeCategory))
            preset = R.id.filter_grains;
        else if ("Others".equalsIgnoreCase(activeCategory))
            preset = R.id.filter_others;

        Chip chip = findViewById(preset);
        if (chip != null) {
            chip.setChecked(true);
        }
    }

    private void fetchProducts() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // When "Farmer Products" is active, fetch only the current farmer's products
        if (farmerProductsOnly) {
            SessionManager sm = new SessionManager(this);
            long farmerId = sm.getUserId();
            if (farmerId <= 0) {
                showToast("Please log in as a farmer to see your products");
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                return;
            }
            apiService.getFarmerProducts(farmerId).enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        allProducts.clear();
                        allProducts.addAll(response.body());
                        applyFilters();
                    } else {
                        Log.e("ProductListActivity", "API Error: " + response.message());
                        showToast("Failed to load your products");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                    Log.e("ProductListActivity", "Network Error: " + t.getMessage());
                    showToast("Cannot connect to backend server");
                }
            });
            return;
        }

        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    allProducts.clear();
                    allProducts.addAll(response.body());
                    applyFilters();
                } else {
                    Log.e("ProductListActivity", "API Error: " + response.message());
                    showToast("Failed to load products from server");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("ProductListActivity", "Network Error: " + t.getMessage());
                showToast("Cannot connect to backend server");
            }
        });
    }

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

        adapter.updateList(filtered);
        updateEmptyState(filtered.size());
    }

    private boolean isKnownCategory(String category) {
        if (category == null)
            return false;
        String c = category.toLowerCase(Locale.US);
        return c.contains("vegetable") || c.contains("fruit") || c.contains("grain") || c.contains("dairy");
    }

    private void updateEmptyState(int count) {
        if (tvCount != null) {
            tvCount.setText(count + (count == 1 ? " product available" : " products available"));
        }
        if (emptyView != null) {
            emptyView.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        }
    }

    // ==================== FILTER SHEET ====================
    private void showFilterSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.ThemeOverlay_Farmer_BottomSheet);
        View content = LayoutInflater.from(this).inflate(R.layout.sheet_filters, null, false);
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
            farmerProductsOnly = false;
            minPrice = null;
            maxPrice = null;
            sortMode = SortMode.RELEVANCE;
            preselectChipForCategory();
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

            preselectChipForCategory();
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

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
        startActivity(intent);
    }

    private void addToCart(Product product) {
        SessionManager sessionManager = new SessionManager(this);
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
