package com.example.farmer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.farmer.R;
import com.example.farmer.activities.AddProductActivity;
import com.example.farmer.activities.EditProfileActivity;
import com.example.farmer.activities.LoginActivity;
import com.example.farmer.activities.OrdersActivity;
import com.example.farmer.activities.settingsActivity;
import com.example.farmer.activities.HelpSupportActivity;
import com.example.farmer.models.Order;
import com.example.farmer.models.User;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Unified profile screen.
 *
 * Previously this fragment read {@code SharedPreferences} directly and used
 * hard-coded fallbacks ("Farmer" / "FARMER"), which displayed the wrong role
 * for buyers and could show a stale/empty email. It now uses the single
 * {@link SessionManager} source of truth, so both farmers and buyers see the
 * same, correct email and role.
 *
 * The screen is role-aware: farmers get their seller tools ("My products"),
 * while both roles keep orders, favorites, settings and help.
 */
public class ProfileFragment extends Fragment {

        private ImageView profilePicture;

        private TextView profileName;
        private TextView userTypeChip;

        private TextView profileEmail;
        private TextView profilePhone;
        private TextView profileLocation;

        private TextView ratingValue;
        private TextView productsSoldValue;
        private TextView followersValue;

        private View myProductsMenu;
        private View myOrdersMenu;
        private View favoritesMenu;
        private View settingsMenu;
        private View helpMenu;

        private Button editProfileButton;
        private Button logoutButton;

        private SessionManager sessionManager;

        public ProfileFragment() {
                // Required empty constructor
        }

        @Nullable
        @Override
        public View onCreateView(
                        @NonNull LayoutInflater inflater,
                        @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
                return inflater.inflate(R.layout.fragment_profile, container, false);
        }

        @Override
        public void onViewCreated(
                        @NonNull View view,
                        @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);

                sessionManager = new SessionManager(requireContext());

                initializeViews(view);
                loadUserProfile();
                setupClickListeners();
        }

        private void initializeViews(View view) {
                profilePicture = view.findViewById(R.id.profile_picture);

                profileName = view.findViewById(R.id.profile_name);
                userTypeChip = view.findViewById(R.id.user_type_chip);

                profileEmail = view.findViewById(R.id.profile_email);
                profilePhone = view.findViewById(R.id.profile_phone);
                profileLocation = view.findViewById(R.id.profile_location);

                ratingValue = view.findViewById(R.id.rating_value);
                productsSoldValue = view.findViewById(R.id.products_sold_value);
                followersValue = view.findViewById(R.id.followers_value);

                myProductsMenu = view.findViewById(R.id.my_products_menu);
                myOrdersMenu = view.findViewById(R.id.my_orders_menu);
                favoritesMenu = view.findViewById(R.id.favorites_menu);
                settingsMenu = view.findViewById(R.id.settings_menu);
                helpMenu = view.findViewById(R.id.help_menu);

                editProfileButton = view.findViewById(R.id.edit_profile_button);
                logoutButton = view.findViewById(R.id.logout_button);
        }

        private void loadUserProfile() {
                String name = sessionManager.getUserName();
                String email = sessionManager.getUserEmail();
                String phone = sessionManager.getUserPhone();
                String userType = sessionManager.getUserType();
                String location = composeLocation();

                boolean isFarmer = "FARMER".equalsIgnoreCase(userType);

                // NAME
                if (profileName != null) {
                        profileName.setText(TextUtils.isEmpty(name) ? getString(R.string.app_name) : name);
                }

                // EMAIL - identical handling for farmer and buyer.
                if (profileEmail != null) {
                        profileEmail.setText(TextUtils.isEmpty(email) ? "Email not available" : email);
                }

                // PHONE
                if (profilePhone != null) {
                        profilePhone.setText(TextUtils.isEmpty(phone) ? "Phone not added" : phone);
                }

                // LOCATION
                if (profileLocation != null) {
                        profileLocation.setText(TextUtils.isEmpty(location) ? "Location not added" : location);
                }

                // ROLE
                if (userTypeChip != null) {
                        userTypeChip.setText(isFarmer ? "FARMER" : "BUYER");
                }

                // ROLE-AWARE ACTIONS: only farmers manage their own listed products.
                if (myProductsMenu != null) {
                        myProductsMenu.setVisibility(isFarmer ? View.VISIBLE : View.GONE);
                }

                // PROFILE IMAGE (use the app's own icon drawable).
                if (profilePicture != null) {
                        profilePicture.setImageResource(R.drawable.ic_person);
                }

                // Default statistics
                if (ratingValue != null) {
                        ratingValue.setText("4.9");
                }
                if (followersValue != null) {
                        followersValue.setText("1.2K");
                }
                if (productsSoldValue != null) {
                        productsSoldValue.setText("0");
                }

                // Refresh from the server so both roles always show current data.
                Long userId = sessionManager.getUserId();
                if (userId > 0) {
                        refreshFromServer(userId);
                }
        }

        /**
         * Builds a human-readable location from the session's city/address so both
         * farmer and buyer profiles render the same way.
         */
        private String composeLocation() {
                String city = sessionManager.getUserCity();
                String address = sessionManager.getUserAddress();

                StringBuilder builder = new StringBuilder();
                if (!TextUtils.isEmpty(address)) {
                        builder.append(address);
                }
                if (!TextUtils.isEmpty(city)) {
                        if (builder.length() > 0) {
                                builder.append(", ");
                        }
                        builder.append(city);
                }
                return builder.toString();
        }

        private void refreshFromServer(Long userId) {
                RetrofitClient.getApiService().getUserProfile(userId)
                                .enqueue(new Callback<User>() {
                                        @Override
                                        public void onResponse(@NonNull Call<User> call,
                                                        @NonNull Response<User> response) {
                                                if (!isAdded() || response.body() == null) {
                                                        return;
                                                }
                                                User user = response.body();
                                                if (profileName != null && !TextUtils.isEmpty(user.getFullName())) {
                                                        profileName.setText(user.getFullName());
                                                }
                                                if (profileEmail != null && !TextUtils.isEmpty(user.getEmail())) {
                                                        profileEmail.setText(user.getEmail());
                                                }
                                                if (profilePhone != null && !TextUtils.isEmpty(user.getPhone())) {
                                                        profilePhone.setText(user.getPhone());
                                                }
                                                if (userTypeChip != null && !TextUtils.isEmpty(user.getUserType())) {
                                                        userTypeChip.setText(user.getUserType().toUpperCase());
                                                }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                                                // Offline: the cached session values already rendered.
                                        }
                                });

                RetrofitClient.getApiService().getUserOrders(userId)
                                .enqueue(new Callback<List<Order>>() {
                                        @Override
                                        public void onResponse(@NonNull Call<List<Order>> call,
                                                        @NonNull Response<List<Order>> response) {
                                                if (!isAdded() || response.body() == null) {
                                                        return;
                                                }
                                                if (productsSoldValue != null) {
                                                        productsSoldValue.setText(String.valueOf(response.body().size()));
                                                }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                                        }
                                });
        }

        private void setupClickListeners() {
                if (myProductsMenu != null) {
                        myProductsMenu.setOnClickListener(
                                        v -> startActivity(new Intent(requireContext(), AddProductActivity.class)));
                }

                if (myOrdersMenu != null) {
                        myOrdersMenu.setOnClickListener(
                                        v -> startActivity(new Intent(requireContext(), OrdersActivity.class)));
                }

                if (favoritesMenu != null) {
                        favoritesMenu.setOnClickListener(v -> Toast
                                        .makeText(requireContext(), "Favorites coming soon", Toast.LENGTH_SHORT)
                                        .show());
                }

                if (settingsMenu != null) {
                        settingsMenu.setOnClickListener(
                                        v -> startActivity(new Intent(requireContext(), settingsActivity.class)));
                }

                if (helpMenu != null) {
                        helpMenu.setOnClickListener(
                                        v -> startActivity(new Intent(requireContext(), HelpSupportActivity.class)));
                }

                if (editProfileButton != null) {
                        editProfileButton.setOnClickListener(
                                        v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
                }

                if (logoutButton != null) {
                        logoutButton.setOnClickListener(v -> logoutUser());
                }
        }

        private void logoutUser() {
                sessionManager.logout();

                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
        }

        @Override
        public void onResume() {
                super.onResume();
                // Reload after EditProfileActivity so changes appear immediately.
                if (sessionManager == null && getContext() != null) {
                        sessionManager = new SessionManager(getContext());
                }
                loadUserProfile();
        }
}
