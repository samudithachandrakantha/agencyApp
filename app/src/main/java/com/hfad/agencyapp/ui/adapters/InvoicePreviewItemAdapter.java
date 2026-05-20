package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.models.InvoicePreviewLineItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvoicePreviewItemAdapter extends RecyclerView.Adapter<InvoicePreviewItemAdapter.ViewHolder> {

    private final List<InvoicePreviewLineItem> items = new ArrayList<>();
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), currencyFormat);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<InvoicePreviewLineItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvProductCode;
        private final TextView tvQuantity;
        private final TextView tvUnitPrice;
        private final TextView tvLineTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_preview_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_preview_product_code);
            tvQuantity = itemView.findViewById(R.id.tv_preview_quantity);
            tvUnitPrice = itemView.findViewById(R.id.tv_preview_unit_price);
            tvLineTotal = itemView.findViewById(R.id.tv_preview_line_total);
        }

        void bind(InvoicePreviewLineItem item, DecimalFormat currencyFormat) {
            tvProductName.setText(item.productName);
            tvProductCode.setText(item.productCode);
            tvQuantity.setText("Qty: " + item.quantity);
            tvUnitPrice.setText("Unit: Rs. " + currencyFormat.format(item.unitPrice));
            tvLineTotal.setText("Rs. " + currencyFormat.format(item.lineTotal));
        }
    }
}