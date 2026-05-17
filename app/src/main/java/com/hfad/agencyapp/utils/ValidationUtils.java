package com.hfad.agencyapp.utils;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_REGEX.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() >= 10;
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.length() >= 2;
    }

    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }

    public static boolean isValidQuantity(int quantity) {
        return quantity > 0;
    }

    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text);
    }

    public static String getErrorMessage(String fieldName, String errorType) {
        switch (errorType) {
            case "empty":
                return fieldName + " is required";
            case "invalid_email":
                return "Invalid email format";
            case "invalid_phone":
                return "Phone number must be at least 10 digits";
            case "invalid_password":
                return "Password must be at least 6 characters";
            case "invalid_name":
                return fieldName + " must be at least 2 characters";
            case "invalid_amount":
                return "Amount must be greater than 0";
            default:
                return "Invalid " + fieldName;
        }
    }
}

