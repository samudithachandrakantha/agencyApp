package com.hfad.agencyapp.ui.profile;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hfad.agencyapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private static final String PROFILE_NAME = "Shanka Distributors";
    private static final String PROFILE_INITIALS = "SD";

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.profileBack.setOnClickListener(v -> finish());

        bindProfile();
    }

    private void bindProfile() {
        binding.tvProfileAvatar.setText(PROFILE_INITIALS);
        binding.tvProfileName.setText(PROFILE_NAME);
    }
}