package com.example.farmer.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String PHONE_PATTERN = "^[0-9]{10}$";

    // Validate email
    public static boolean isValidEmail(String email) {
        return Pattern.matches(EMAIL_PATTERN, email);
    }

    // Validate phone
    public static boolean isValidPhone(String phone) {
        return Pattern.matches(PHONE_PATTERN, phone);
    }

    // Validate password
    public static boolean isValidPassword(String password) {
        // At least 6 characters
        return password.length() >= 6;
    }

    // Validate empty field
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    // Validate price
    public static boolean isValidPrice(String price) {
        try {
            double p = Double.parseDouble(price);
            return p > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate quantity
    public static boolean isValidQuantity(String quantity) {
        try {
            int q = Integer.parseInt(quantity);
            return q > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
