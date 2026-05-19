package com.hfad.agencyapp.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        binding.tvProductName.setText(product.name);
        binding.tvCategoryName.setText(product.categoryName != null ? product.categoryName : ("Category #" + product.categoryId));
        binding.tvPrice.setText(String.format(Locale.getDefault(), "Rs. %.2f", product.sellingPrice));
        binding.tvStockChip.setText(product.stock == 0 ? "Out of Stock" : "Stock: " + product.stock);
        binding.tvStockChip.setBackgroundResource(getStockBackground(product.stock, product.lowStockThreshold));

        if (product.imagePath != null && !product.imagePath.isEmpty()) {
            Glide.with(binding.getRoot().getContext())
                    .load(product.imagePath)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.ivProduct);
        } else {
            binding.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

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


