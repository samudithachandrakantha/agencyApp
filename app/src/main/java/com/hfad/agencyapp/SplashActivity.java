package com.hfad.agencyapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.hfad.agencyapp.databinding.ActivitySplashBinding;
import com.hfad.agencyapp.ui.dashboard.DashboardActivity;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private ValueAnimator progressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configure system status bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary));

        startLoadingAnimation();
    }

    private void startLoadingAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(2500); // 2.5 seconds simulation
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            updateProgress(progress);
        });
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigateToMain();
            }
        });
        progressAnimator.start();
    }

    private void updateProgress(int progress) {
        binding.tvProgressPercent.setText(progress + "%");
        
        // Update horizontal bias of the truck progress indicator
        float bias = progress / 100f;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.ivTruckProgress.getLayoutParams();
        params.horizontalBias = bias;
        binding.ivTruckProgress.setLayoutParams(params);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        super.onDestroy();
    }
}
