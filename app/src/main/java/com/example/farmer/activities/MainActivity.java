package com.example.farmer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.farmer.activities.SettingsActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.farmer.R;
import com.example.farmer.fragments.HomeFragment;
import com.example.farmer.fragments.ProfileFragment;
import com.example.farmer.fragments.OrdersFragment;
import com.example.farmer.fragments.CartFragment;

import com.example.farmer.models.User;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Updated Navigation Logic for all sections
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (id == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            return loadFragment(selectedFragment);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        if (savedInstanceState == null) {
            if ("cart".equals(getIntent().getStringExtra("open_tab"))) {
                bottomNavigationView.setSelectedItemId(R.id.nav_cart);
            } else if ("orders".equals(getIntent().getStringExtra("open_tab"))) {
                bottomNavigationView.setSelectedItemId(R.id.nav_orders);
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }

        if (sessionManager.isLoggedIn()) {
            updateHeaderWithCache();
            fetchUserProfile(sessionManager.getUserId());
        }

        // Sidebar Navigation Listener Updated
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                handleLogout();
            } else if (id == R.id.nav_farmer_dashboard) {
                startActivity(new Intent(MainActivity.this, FarmerDashboardActivity.class));
            } else if (id == R.id.nav_settings) {
                // FIX: Changed lowercase 'settingsActivity' to standard 'SettingsActivity'
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(MainActivity.this, HelpSupportActivity.class));
            } else if (id == R.id.nav_browse) {
                startActivity(new Intent(MainActivity.this, ProductListActivity.class));
            } else {
                // Synchronize sidebar clicks with bottom navigation
                bottomNavigationView.setSelectedItemId(id);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, ProductListActivity.class));
            return true;
        } else if (id == R.id.action_cart) {
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void updateHeaderWithCache() {
        View header = navigationView.getHeaderView(0);
        if (header != null) {
            TextView tvName = header.findViewById(R.id.nav_user_name);
            TextView tvEmail = header.findViewById(R.id.nav_user_email);
            if (tvName != null)
                tvName.setText(sessionManager.getUserName());
            if (tvEmail != null)
                tvEmail.setText(sessionManager.getUserEmail());
        }
    }

    private void fetchUserProfile(Long userId) {
        RetrofitClient.getApiService().getUserProfile(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateNavHeader(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                // FIX: Added logging to diagnose network connection problems
                Log.e(TAG, "Failed to fetch user profile data", t);
            }
        });
    }

    private void updateNavHeader(User user) {
        View header = navigationView.getHeaderView(0);
        if (header != null) {
            TextView tvName = header.findViewById(R.id.nav_user_name);
            TextView tvEmail = header.findViewById(R.id.nav_user_email);
            if (tvName != null)
                tvName.setText(user.getFullName());
            if (tvEmail != null)
                tvEmail.setText(user.getEmail());
        }
    }

    private void handleLogout() {
        sessionManager.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}