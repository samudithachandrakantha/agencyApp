package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ItemProductBinding;

public class ProductAdapter extends ListAdapter<Product, ProductViewHolder> {

    public interface OnProductClickListener {
        void onClick(Product product);
        void onLongClick(Product product, View anchor);
        void onEditClick(Product product);
    }

    private final OnProductClickListener listener;

    public ProductAdapter(OnProductClickListener listener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                return oldItem.name.equals(newItem.name)
                        && safeEquals(oldItem.sku, newItem.sku)
                        && oldItem.stock == newItem.stock
                        && oldItem.sellingPrice == newItem.sellingPrice
                        && safeEquals(oldItem.imagePath, newItem.imagePath);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}

