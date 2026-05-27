package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.models.RecentInvoiceUiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentInvoiceAdapter extends RecyclerView.Adapter<RecentInvoiceAdapter.InvoiceViewHolder> {

    private final List<RecentInvoiceUiModel> items = new ArrayList<>();
    private final List<RecentInvoiceUiModel> originalItems = new ArrayList<>();
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(long invoiceDbId);
    }

    public void setOnInvoiceClickListener(OnInvoiceClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        RecentInvoiceUiModel model = items.get(position);
        holder.bind(model);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && model.invoiceDbId > 0) {
                listener.onInvoiceClick(model.invoiceDbId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<RecentInvoiceUiModel> data) {
        items.clear();
        originalItems.clear();
        if (data != null) {
            items.addAll(data);
            originalItems.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        items.clear();
        if (query == null || query.trim().isEmpty()) {
            items.addAll(originalItems);
        } else {
            String queryLower = query.toLowerCase(Locale.US).trim();
            for (RecentInvoiceUiModel item : originalItems) {
                if (item.customerName.toLowerCase(Locale.US).contains(queryLower)
                        || item.invoiceId.toLowerCase(Locale.US).contains(queryLower)
                        || item.totalAmount.toLowerCase(Locale.US).contains(queryLower)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCustomer;
        private final TextView tvInvoiceId;
        private final TextView tvAmount;
        private final TextView tvStatus;
        private final LinearLayout pendingContainer;
        private final TextView tvDuePayment;
        private final TextView tvPendingLabel;

        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomer = itemView.findViewById(R.id.tvProductId);
            tvInvoiceId = itemView.findViewById(R.id.tvQuantity);
            tvAmount = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvUnitPrice);
            pendingContainer = itemView.findViewById(R.id.pendingContainer);
            tvDuePayment = itemView.findViewById(R.id.tvDuePayment);
            tvPendingLabel = itemView.findViewById(R.id.tvPendingLabel);
        }

        void bind(RecentInvoiceUiModel item) {
            tvCustomer.setText(item.customerName);
            tvInvoiceId.setText(item.invoiceId);
            tvAmount.setText(item.totalAmount);
            
            // Handle cheque date display
            if (item.chequeDate != null && !item.chequeDate.isEmpty()) {
                tvStatus.setVisibility(View.GONE);
                pendingContainer.setVisibility(View.VISIBLE);
                tvDuePayment.setText("Cheque Date: " + item.chequeDate);
                return;
            }
            
            // Handle pending status with due payment display
            if (item.isPending && item.dueAmount != null && !item.dueAmount.isEmpty()) {
                tvStatus.setVisibility(View.GONE);
                pendingContainer.setVisibility(View.VISIBLE);
                tvDuePayment.setText("Due: Rs. " + item.dueAmount);
                return;
            }
            
            pendingContainer.setVisibility(View.GONE);
            
            String rawStatus = item.paymentStatus == null ? "" : item.paymentStatus.trim();
            if (rawStatus.isEmpty()) {
                tvStatus.setVisibility(View.GONE);
                tvStatus.setText("");
                return;
            }

            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(rawStatus);

            String status = rawStatus.toLowerCase(Locale.US);
            if ("paid".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_paid_text));
            } else if ("cash".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_cash);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_cash_text));
            } else if ("pending".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_cheque);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_cheque_text));
            } else if ("credit".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_credit);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_credit_text));
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_status_cheque);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_cheque_text));
            }
        }
    }
}


