package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.models.ProductSaleRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ProductSalesAdapter extends ListAdapter<ProductSaleRecord, ProductSalesAdapter.SaleViewHolder> {

    private static final DiffUtil.ItemCallback<ProductSaleRecord> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProductSaleRecord>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProductSaleRecord oldItem, @NonNull ProductSaleRecord newItem) {
            return oldItem.soldAt == newItem.soldAt
                    && oldItem.quantity == newItem.quantity
                    && Double.compare(oldItem.revenue, newItem.revenue) == 0
                    && Objects.equals(oldItem.shopName, newItem.shopName)
                    && Objects.equals(oldItem.invoiceNumber, newItem.invoiceNumber);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductSaleRecord oldItem, @NonNull ProductSaleRecord newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    };

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public ProductSalesAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_sale, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        holder.bind(getItem(position), dateFormat);
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvShopName;
        private final TextView tvInvoiceNumber;
        private final TextView tvDate;
        private final TextView tvQuantity;
        private final TextView tvRevenue;

        SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvSaleShopName);
            tvInvoiceNumber = itemView.findViewById(R.id.tvSaleInvoiceNumber);
            tvDate = itemView.findViewById(R.id.tvSaleDate);
            tvQuantity = itemView.findViewById(R.id.tvSaleQuantity);
            tvRevenue = itemView.findViewById(R.id.tvSaleRevenue);
        }

        void bind(ProductSaleRecord item, SimpleDateFormat dateFormat) {
            tvShopName.setText(item.shopName != null && !item.shopName.trim().isEmpty() ? item.shopName : itemView.getContext().getString(R.string.unknown_value));
            tvInvoiceNumber.setText(item.invoiceNumber != null && !item.invoiceNumber.trim().isEmpty() ? item.invoiceNumber : itemView.getContext().getString(R.string.unknown_value));
            tvDate.setText(dateFormat.format(new Date(item.soldAt > 0 ? item.soldAt : System.currentTimeMillis())));
            tvQuantity.setText(itemView.getContext().getString(R.string.product_sale_quantity_value, item.quantity));
            tvRevenue.setText(itemView.getContext().getString(R.string.amount_format, String.format(Locale.getDefault(), "%.2f", item.revenue)));
        }
    }

}