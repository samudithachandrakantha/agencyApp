package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.data.entities.StockMovement;
import com.hfad.agencyapp.databinding.ItemStockMovementBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StockMovementAdapter extends ListAdapter<StockMovement, StockMovementAdapter.VH> {

    public StockMovementAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull StockMovement oldItem, @NonNull StockMovement newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull StockMovement oldItem, @NonNull StockMovement newItem) {
                return oldItem.quantity == newItem.quantity
                        && safeEquals(oldItem.type, newItem.type)
                        && safeEquals(oldItem.reason, newItem.reason)
                        && safeEquals(oldItem.notes, newItem.notes);
            }
        });
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemStockMovementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    public static class VH extends RecyclerView.ViewHolder {
        private final ItemStockMovementBinding binding;

        VH(ItemStockMovementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StockMovement movement) {
            binding.tvType.setText(movement.type);
            binding.tvQty.setText(binding.getRoot().getContext().getString(
                    com.hfad.agencyapp.R.string.stock_movement_qty,
                    "IN".equals(movement.type) ? "+" : "-",
                    movement.quantity));
            binding.tvReason.setText(movement.reason == null ? "" : movement.reason);
            binding.tvNotes.setText(movement.notes == null || movement.notes.isEmpty() ? "" : movement.notes);
            binding.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(movement.createdAt)));
        }
    }

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}






