package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products = new ArrayList<>();
    private OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void submitList(List<Product> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPrice;
        private final TextView tvSku;
        private final ImageView ivProduct;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSku = itemView.findViewById(R.id.tvSku);
            ivProduct = itemView.findViewById(R.id.ivProduct);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onProductClick(products.get(pos));
                }
            });
        }

        void bind(Product product) {
            tvName.setText(product.name);
            tvPrice.setText(String.format("Rs. %.2f", product.price));
            tvSku.setText("SKU: " + product.sku);

            if (product.imageUrl != null && !product.imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(ivProduct);
            } else {
                ivProduct.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
}

