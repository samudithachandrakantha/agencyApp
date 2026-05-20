package com.hfad.agencyapp.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ItemProductBinding;

import java.util.Locale;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private final ItemProductBinding binding;

    public ProductViewHolder(ItemProductBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    void bind(@NonNull Product product, @NonNull ProductAdapter.OnProductClickListener listener) {
        binding.tvProductName.setText(product.name != null ? product.name : "");
        String brandLine = product.brand != null && !product.brand.trim().isEmpty()
                ? product.brand
                : binding.getRoot().getContext().getString(R.string.brand_not_set);
        binding.tvCategoryName.setText(brandLine);
        binding.tvPrice.setText(String.format(Locale.getDefault(), "Rs. %.2f", product.sellingPrice));
        binding.tvStockChip.setText(product.stock == 0 ? "Out of Stock" : "Stock: " + product.stock);
        binding.tvStockChip.setBackgroundResource(getStockBackground(product.stock, product.lowStockThreshold));

        binding.getRoot().setOnClickListener(v -> listener.onClick(product));
        binding.getRoot().setOnLongClickListener(v -> {
            listener.onLongClick(product, v);
            return true;
        });
        binding.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
    }

    private int getStockBackground(int stock, int threshold) {
        if (stock == 0) return R.drawable.bg_stock_chip_dark_red;
        if (stock < threshold) return R.drawable.bg_stock_chip_red;
        return R.drawable.bg_stock_chip_green;
    }
}
