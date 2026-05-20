package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.hfad.agencyapp.ui.models.InvoiceItem;
import com.hfad.agencyapp.databinding.ItemRowInvoiceBinding;
import java.text.DecimalFormat;

/**
 * RecyclerView adapter for displaying invoice items.
 * Uses ViewBinding and DiffUtil for efficient updates.
 */
public class InvoiceItemsAdapter extends ListAdapter<InvoiceItem, InvoiceItemsAdapter.InvoiceItemViewHolder> {

    private final OnItemActionListener listener;
    private final DecimalFormat currencyFormat;

    public interface OnItemActionListener {
        void onQuantityChange(int position, int newQuantity);
        void onRemove(int position);
    }

    public InvoiceItemsAdapter(OnItemActionListener listener) {
        super(new InvoiceItemDiffCallback());
        this.listener = listener;
        this.currencyFormat = new DecimalFormat("#,##0.00");
    }

    @NonNull
    @Override
    public InvoiceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRowInvoiceBinding binding = ItemRowInvoiceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new InvoiceItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceItemViewHolder holder, int position) {
        InvoiceItem item = getItem(position);
        if (item != null) {
            holder.bind(item, position, listener);
        }
    }

    /**
     * ViewHolder for individual invoice item rows.
     */
    static class InvoiceItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemRowInvoiceBinding binding;
        private final DecimalFormat currencyFormat;

        InvoiceItemViewHolder(ItemRowInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.currencyFormat = new DecimalFormat("#,##0.00");
        }

        void bind(InvoiceItem item, int position, OnItemActionListener listener) {
            // Product Name
            binding.tvProductName.setText(item.getProductName());

            // Quantity
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Unit Price
            binding.tvUnitPrice.setText("Rs. " + currencyFormat.format(item.getUnitPrice()) + 
                    " × " + item.getQuantity());

            // Line Total (with discount applied)
            binding.tvLineTotal.setText("Rs. " + currencyFormat.format(item.getLineTotal()));

            // Minus Button
            binding.btnMinus.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity >= 1) {
                    listener.onQuantityChange(position, newQuantity);
                }
            });

            // Plus Button
            binding.btnPlus.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                listener.onQuantityChange(position, newQuantity);
            });

            // Delete Button
            binding.btnDelete.setOnClickListener(v -> listener.onRemove(position));
        }
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates.
     */
    private static class InvoiceItemDiffCallback extends DiffUtil.ItemCallback<InvoiceItem> {
        @Override
        public boolean areItemsTheSame(@NonNull InvoiceItem oldItem, @NonNull InvoiceItem newItem) {
            return oldItem.getProductId().equals(newItem.getProductId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull InvoiceItem oldItem, @NonNull InvoiceItem newItem) {
            return oldItem.getProductId().equals(newItem.getProductId()) &&
                    oldItem.getQuantity() == newItem.getQuantity() &&
                    Double.compare(oldItem.getUnitPrice(), newItem.getUnitPrice()) == 0 &&
                    Double.compare(oldItem.getDiscountPercent(), newItem.getDiscountPercent()) == 0;
        }
    }
}


