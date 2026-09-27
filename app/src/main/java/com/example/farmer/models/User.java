package com.example.farmer.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * User model representing a user for the remote PostgreSQL backend.
 * Room annotations have been removed as the app now uses a remote database.
 */
public class User implements Serializable {

    @SerializedName(value = "userId", alternate = {"id"})
    public Long userId;

    @SerializedName("userType")
    public String userType = "BUYER";

    @SerializedName("fullName")
    public String fullName = "";

    @SerializedName("email")
    public String email = "";

    @SerializedName("phone")
    public String phone = "";

    @SerializedName("password") // Matches the 'password' field in your backend requests
    public String passwordHash = "";

    @SerializedName("address")
    public String address = "";

    @SerializedName("city")
    public String city = "";

    @SerializedName("profileImage")
    public String profileImage = "";

    @SerializedName("createdAt")
    private String createdAt = String.valueOf(System.currentTimeMillis());

    // Required empty constructor for Gson and Retrofit
    public User() {
    }

    /**
     * Constructor used by RegistrationActivity to specify the user role.
     */
    public User(String fullName, String email, String passwordHash, String userType) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    // --- Getters and Setters ---

    public Long getUserId() {
        return userId != null ? userId : 0L;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}