package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCustomer;
        private final TextView tvInvoiceId;
        private final TextView tvAmount;
        private final TextView tvStatus;

        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomer = itemView.findViewById(R.id.tvProductId);
            tvInvoiceId = itemView.findViewById(R.id.tvQuantity);
            tvAmount = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvUnitPrice);
        }

        void bind(RecentInvoiceUiModel item) {
            tvCustomer.setText(item.customerName);
            tvInvoiceId.setText(item.invoiceId);
            tvAmount.setText(item.totalAmount);
            tvStatus.setText(item.paymentStatus);

            String status = item.paymentStatus.toLowerCase(Locale.US);
            if ("paid".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_paid_text));
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


