package com.example.farmer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemRemoveListener {
        void onRemove(CartItem item);
    }

    public interface OnCartQuantityChangeListener {
        void onQuantityChange(CartItem item, int newQuantity);
    }

    private List<CartItem> cartItems;
    private final OnCartItemRemoveListener removeListener;
    private OnCartQuantityChangeListener quantityChangeListener;

    public CartAdapter(List<CartItem> cartItems, OnCartItemRemoveListener removeListener) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.removeListener = removeListener;
    }

    public void setOnCartQuantityChangeListener(OnCartQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        String name = item.getProductName();
        if (name == null || name.trim().isEmpty()) {
            name = "Product #" + item.getCartItemId();
        }
        holder.tvName.setText(name);

        double unitPrice = item.getPrice();
        int quantity = item.getQuantity();
        double subtotal = unitPrice * quantity;

        holder.tvPrice.setText(String.format("₹ %.2f", unitPrice));
        holder.tvQuantity.setText(String.valueOf(quantity));
        holder.tvSubtotal.setText(String.format("Total: ₹ %.2f", subtotal));

        holder.btnMinus.setEnabled(quantity > 1);
        holder.btnMinus.setAlpha(quantity > 1 ? 1f : 0.4f);

        holder.btnPlus.setOnClickListener(v -> {
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChange(item, quantity + 1);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (quantity > 1 && quantityChangeListener != null) {
                quantityChangeListener.onQuantityChange(item, quantity - 1);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateList(List<CartItem> newItems) {
        this.cartItems = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity, tvSubtotal;
        android.widget.Button btnMinus, btnPlus;
        ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.cart_item_name);
            tvPrice = itemView.findViewById(R.id.cart_item_price);
            tvQuantity = itemView.findViewById(R.id.cart_item_quantity);
            tvSubtotal = itemView.findViewById(R.id.cart_item_subtotal);
            btnMinus = itemView.findViewById(R.id.cart_item_minus);
            btnPlus = itemView.findViewById(R.id.cart_item_plus);
            btnRemove = itemView.findViewById(R.id.cart_item_remove);
        }
    }
}
