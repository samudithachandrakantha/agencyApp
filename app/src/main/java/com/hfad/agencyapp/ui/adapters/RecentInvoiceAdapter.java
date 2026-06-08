package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.models.RecentInvoiceUiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentInvoiceAdapter extends ListAdapter<RecentInvoiceUiModel, RecentInvoiceAdapter.InvoiceViewHolder> {

    private List<RecentInvoiceUiModel> originalItems = new ArrayList<>();
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(long invoiceDbId);
    }

    public void setOnInvoiceClickListener(OnInvoiceClickListener l) {
        this.listener = l;
    }

    public RecentInvoiceAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull RecentInvoiceUiModel oldItem, @NonNull RecentInvoiceUiModel newItem) {
                return oldItem.invoiceDbId == newItem.invoiceDbId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull RecentInvoiceUiModel oldItem, @NonNull RecentInvoiceUiModel newItem) {
                return java.util.Objects.equals(oldItem.customerName, newItem.customerName)
                        && java.util.Objects.equals(oldItem.invoiceId, newItem.invoiceId)
                        && java.util.Objects.equals(oldItem.totalAmount, newItem.totalAmount)
                        && java.util.Objects.equals(oldItem.paymentStatus, newItem.paymentStatus)
                        && java.util.Objects.equals(oldItem.dueAmount, newItem.dueAmount)
                        && oldItem.isPending == newItem.isPending
                        && java.util.Objects.equals(oldItem.chequeDate, newItem.chequeDate);
            }
        });
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        RecentInvoiceUiModel model = getItem(position);
        holder.bind(model);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && model.invoiceDbId > 0) {
                listener.onInvoiceClick(model.invoiceDbId);
            }
        });
    }

    @Override
    public void submitList(List<RecentInvoiceUiModel> data) {
        if (data != null) {
            originalItems = new ArrayList<>(data);
        } else {
            originalItems = new ArrayList<>();
        }
        super.submitList(data);
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            super.submitList(originalItems);
        } else {
            String queryLower = query.toLowerCase(Locale.US).trim();
            List<RecentInvoiceUiModel> filtered = new ArrayList<>();
            for (RecentInvoiceUiModel item : originalItems) {
                if (item.customerName.toLowerCase(Locale.US).contains(queryLower)
                        || item.invoiceId.toLowerCase(Locale.US).contains(queryLower)
                        || item.totalAmount.toLowerCase(Locale.US).contains(queryLower)) {
                    filtered.add(item);
                }
            }
            super.submitList(filtered);
        }
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
            tvCustomer = itemView.findViewById(R.id.tvCustomerName);
            tvInvoiceId = itemView.findViewById(R.id.tvInvoiceNumberAndDate);
            tvAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvPaymentStatus);
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
                tvDuePayment.setText(itemView.getContext().getString(R.string.cheque_date_label, item.chequeDate));
            } else if (item.isPending && item.dueAmount != null && !item.dueAmount.isEmpty()) {
                tvStatus.setVisibility(View.GONE);
                pendingContainer.setVisibility(View.VISIBLE);
                tvDuePayment.setText(itemView.getContext().getString(R.string.due_amount_label, item.dueAmount));
            } else {
                pendingContainer.setVisibility(View.GONE);
            }
            
            String rawStatus = item.paymentStatus == null ? "" : item.paymentStatus.trim();
            if (rawStatus.isEmpty() || (pendingContainer.getVisibility() == View.VISIBLE)) {
                tvStatus.setVisibility(View.GONE);
            } else {
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
}
