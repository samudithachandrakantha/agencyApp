package com.hfad.agencyapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.agencyapp.databinding.ItemCustomerBinding;
import com.hfad.agencyapp.ui.models.Customer;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Adapter for customer list using ListAdapter + DiffUtil and ViewBinding.
 */
public class CustomersAdapter extends ListAdapter<Customer, RecyclerView.ViewHolder> {

    public interface OnCustomerActionListener {
        void onClick(Customer customer);
        void onLongClick(Customer customer, View anchor);
    }

    private final OnCustomerActionListener listener;

    public CustomersAdapter(OnCustomerActionListener listener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull Customer oldItem, @NonNull Customer newItem) {
                return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Customer oldItem, @NonNull Customer newItem) {
                return Objects.equals(oldItem.getBusinessName(), newItem.getBusinessName())
                        && Objects.equals(oldItem.getContactPerson(), newItem.getContactPerson())
                        && Objects.equals(oldItem.getAddress(), newItem.getAddress())
                        && Objects.equals(oldItem.getPaymentMethods(), newItem.getPaymentMethods())
                        && oldItem.isBlocked() == newItem.isBlocked()
                        && Double.compare(oldItem.getOutstandingAmount(), newItem.getOutstandingAmount()) == 0;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerBinding binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Customer c = getItem(position);
        ((CustomerViewHolder) holder).bind(c, listener);
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        private final ItemCustomerBinding binding;

        CustomerViewHolder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Customer c, OnCustomerActionListener listener) {
            binding.tvBusinessName.setText(c.getBusinessName());
            binding.tvContactPerson.setText(c.getContactPerson());
            binding.tvAddress.setText(c.getAddress());

            binding.tvOutstandingLabel.setText(com.hfad.agencyapp.R.string.outstanding_label);
            DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
            binding.tvOutstandingAmount.setText(itemView.getContext().getString(
                    com.hfad.agencyapp.R.string.amount_format,
                    amountFormat.format(c.getOutstandingAmount())
            ));
            binding.tvOutstandingAmount.setTextColor(c.getOutstandingAmount() > 0
                    ? itemView.getContext().getColor(com.hfad.agencyapp.R.color.error_red)
                    : itemView.getContext().getColor(com.hfad.agencyapp.R.color.soft_green_icon));

            // Apply blocked styling if customer is blocked
            if (c.isBlocked()) {
                itemView.setAlpha(0.5f);
            } else {
                itemView.setAlpha(1.0f);
            }

            binding.getRoot().setOnClickListener(v -> listener.onClick(c));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onLongClick(c, v);
                return true;
            });
        }
    }
}

