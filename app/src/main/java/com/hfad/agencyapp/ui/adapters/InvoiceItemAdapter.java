package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.InvoiceItem;

import java.util.ArrayList;
import java.util.List;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.InvoiceItemViewHolder> {

    private final List<InvoiceItem> items = new ArrayList<>();

    @NonNull
    @Override
    public InvoiceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceItemViewHolder holder, int position) {
        InvoiceItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<InvoiceItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    static class InvoiceItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductId;
        private final TextView tvQuantity;
        private final TextView tvUnitPrice;
        private final TextView tvTotalPrice;

        InvoiceItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductId = itemView.findViewById(R.id.tvProductId);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }

        void bind(InvoiceItem item) {
            android.content.Context context = itemView.getContext();
            tvProductId.setText(context.getString(R.string.product_id_label, item.productId));
            tvQuantity.setText(context.getString(R.string.quantity_label_short, item.quantity));
            tvUnitPrice.setText(context.getString(R.string.price_label_currency, String.format(java.util.Locale.getDefault(), "%.2f", item.unitPrice)));
            tvTotalPrice.setText(context.getString(R.string.total_price_label, String.format(java.util.Locale.getDefault(), "%.2f", item.totalPrice)));
        }
    }
}

