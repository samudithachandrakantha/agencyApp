package com.hfad.agencyapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.agencyapp.databinding.ActivityLoginBinding;
import com.hfad.agencyapp.ui.dashboard.DashboardActivity;
import com.hfad.agencyapp.utils.ValidationUtils;
import com.hfad.agencyapp.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check if already logged in
        if (viewModel.getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.btnSignUp.setOnClickListener(v -> handleSignUp());

        // FOR TESTING: Add demo/skip button
        binding.btnSignUp.setText("Demo (Skip Auth)");
        binding.btnSignUp.setOnClickListener(v -> {
            // Skip Firebase auth and go directly to dashboard for testing
            Toast.makeText(this, "Entering demo mode...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        viewModel.userLive.observe(this, user -> {
            binding.progress.setVisibility(View.GONE);
            if (user != null) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        });

        viewModel.errorMessage.observe(this, error -> {
            binding.progress.setVisibility(View.GONE);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.loading.observe(this, isLoading -> {
            binding.progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
            binding.btnSignUp.setEnabled(!isLoading);
        });
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        // DEMO MODE: Skip validation and go to dashboard
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Demo Mode: Entering Dashboard...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        // Optional: Try real authentication if email/password provided
        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.setError("Invalid email format");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.etPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Try Firebase authentication
        viewModel.signIn(email, password);
    }

    private void handleSignUp() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email required");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.etEmail.setError("Invalid email format");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password required");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.etPassword.setError("Password must be at least 6 characters");
            return;
        }

        viewModel.signUp(email, password);
    }
}

