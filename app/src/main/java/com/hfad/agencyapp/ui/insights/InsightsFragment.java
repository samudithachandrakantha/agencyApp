package com.hfad.agencyapp.ui.insights;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.agencyapp.databinding.ActivityInsightsBinding;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

public class InsightsFragment extends Fragment {

    private ActivityInsightsBinding binding;
    private DashboardViewModel viewModel;
    private InsightsDashboardBinder binder;

    public static InsightsFragment newInstance() {
        return new InsightsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        if (binding.includeBottomNav != null) {
            binding.includeBottomNav.getRoot().setVisibility(View.GONE);
        }
        binder = new InsightsDashboardBinder(binding, viewModel, getViewLifecycleOwner(), requireContext());
        binder.attach();
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() instanceof com.hfad.agencyapp.ui.tabs.MainTabsActivity) {
                ((com.hfad.agencyapp.ui.tabs.MainTabsActivity) getActivity()).switchToTab(com.hfad.agencyapp.ui.tabs.MainTabsActivity.TAB_HOME);
            } else {
                startActivity(com.hfad.agencyapp.ui.tabs.MainTabsActivity.createIntent(requireContext(), com.hfad.agencyapp.ui.tabs.MainTabsActivity.TAB_HOME));
                requireActivity().finish();
            }
        });
        // Ensure the toolbar shows the Insights title in white on navy background when hosted as a fragment
        binding.toolbar.setTitle("Insights");
        try {
            binding.toolbar.setTitleTextColor(requireContext().getColor(R.color.white));
        } catch (Exception ignored) {
            binding.toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        }
    }
}
