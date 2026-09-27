package com.example.farmer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    // Initialize with an empty list to prevent NullPointerExceptions
    private List<Order> orderList = new ArrayList<>();
    private OnOrderStatusChangeListener onOrderStatusChangeListener;

    public interface OnOrderStatusChangeListener {
        void onStatusChange(Order order, String newStatus);
    }

    // Single constructor to handle initialization
    public OrderAdapter(List<Order> orderList) {
        if (orderList != null) {
            this.orderList = orderList;
        }
    }

    public void setOnOrderStatusChangeListener(OnOrderStatusChangeListener listener) {
        this.onOrderStatusChangeListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use parent context to avoid null context issues
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTextView.setText("Order #" + order.getOrderId());
        holder.quantityTextView.setText("Qty: " + order.getQuantity());
        holder.priceTextView.setText("₹ " + String.format("%.2f", order.getTotalPrice()));
        holder.statusTextView.setText("Status: " + order.getOrderStatus());
        holder.addressTextView.setText("Delivery: " + order.getDeliveryAddress());

        setStatusColor(holder.statusTextView, order.getOrderStatus());

        holder.updateStatusButton.setOnClickListener(v -> {
            String newStatus = getNextStatus(order.getOrderStatus());
            if (onOrderStatusChangeListener != null) {
                onOrderStatusChangeListener.onStatusChange(order, newStatus);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Defensive check: return 0 if list is null
        return orderList != null ? orderList.size() : 0;
    }

    public void updateList(List<Order> newList) {
        // Ensure the list is never set to null
        this.orderList = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ... (rest of your helper methods: getNextStatus, setStatusColor)

    private String getNextStatus(String currentStatus) {
        if (currentStatus == null)
            return "PROCESSING";
        switch (currentStatus.toUpperCase()) {
            // Backend creates orders with status "PLACED"; treat it as pending.
            case "PLACED":
            case "PENDING":
                return "PROCESSING";
            case "PROCESSING":
                return "SHIPPED";
            case "SHIPPED":
                return "DELIVERED";
            default:
                return currentStatus;
        }
    }

    private void setStatusColor(TextView statusTextView, String status) {
        if (context == null || status == null)
            return;
        int color;
        switch (status.toUpperCase()) {
            case "PLACED":
            case "PENDING":
                color = android.R.color.holo_orange_light;
                break;
            case "PROCESSING":
                color = android.R.color.holo_blue_light;
                break;
            case "SHIPPED":
                color = android.R.color.holo_green_light;
                break;
            case "DELIVERED":
                color = android.R.color.darker_gray;
                break;
            default:
                color = android.R.color.black;
        }
        statusTextView.setTextColor(ContextCompat.getColor(context, color));
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, quantityTextView, priceTextView, statusTextView, addressTextView;
        Button updateStatusButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id);
            quantityTextView = itemView.findViewById(R.id.order_quantity);
            priceTextView = itemView.findViewById(R.id.order_price);
            statusTextView = itemView.findViewById(R.id.order_status);
            addressTextView = itemView.findViewById(R.id.order_address);
            updateStatusButton = itemView.findViewById(R.id.update_status_button);
        }
    }
}