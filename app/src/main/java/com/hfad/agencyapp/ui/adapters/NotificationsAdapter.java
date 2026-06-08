package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.models.NotificationUiModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private final List<NotificationUiModel> items = new ArrayList<>();

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<NotificationUiModel> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvBadge;
        private final TextView tvAmount;
        private final TextView tvChequeDate;
        private final TextView tvClearanceDate;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvSubtitle = itemView.findViewById(R.id.tv_notif_subtitle);
            tvBadge = itemView.findViewById(R.id.tv_notif_badge);
            tvAmount = itemView.findViewById(R.id.tv_notif_amount);
            tvChequeDate = itemView.findViewById(R.id.tv_notif_cheque_date);
            tvClearanceDate = itemView.findViewById(R.id.tv_notif_clearance_date);
        }

        void bind(NotificationUiModel item) {
            tvTitle.setText(item.title);
            tvSubtitle.setText(itemView.getContext().getString(R.string.invoice_subtitle_format, item.invoiceNumber, item.customerName));
            tvAmount.setText(item.amount);
            tvChequeDate.setText(itemView.getContext().getString(R.string.cheque_date_label, item.chequeDateFormatted));
            tvClearanceDate.setText(itemView.getContext().getString(R.string.clearance_scheduled_label, item.clearanceDateFormatted));

            tvBadge.setText(item.status);

            // Dynamically style status badge
            if ("UPCOMING".equalsIgnoreCase(item.status)) {
                tvBadge.setBackgroundResource(R.drawable.bg_status_cheque);
                tvBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_cheque_text));
            } else if ("DUE TODAY".equalsIgnoreCase(item.status)) {
                tvBadge.setBackgroundResource(R.drawable.bg_status_credit);
                tvBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_credit_text));
            } else if ("CLEARED".equalsIgnoreCase(item.status)) {
                tvBadge.setBackgroundResource(R.drawable.bg_status_paid);
                tvBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_paid_text));
            } else if ("OVERDUE".equalsIgnoreCase(item.status)) {
                tvBadge.setBackgroundResource(R.drawable.bg_status_overdue);
                tvBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_red));
            } else {
                tvBadge.setBackgroundResource(R.drawable.bg_status_cheque);
                tvBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_cheque_text));
            }
        }
    }
}
