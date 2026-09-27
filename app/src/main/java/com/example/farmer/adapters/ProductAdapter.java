package com.example.farmer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.farmer.R;
import com.example.farmer.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private List<Product> productList = new ArrayList<>();
    private final OnAddToCartClickListener cartListener;
    private OnProductClickListener productClickListener;

    public interface OnAddToCartClickListener {
        void onAddToCart(Product product);
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, OnAddToCartClickListener cartListener) {
        this.context = context;
        this.cartListener = cartListener;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.productClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);

        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position) {

        Product p = productList.get(position);

        holder.name.setText(p.getProductName());

        holder.price.setText(
                "₹ " + String.format("%.2f", p.getPrice()));

        holder.category.setText(p.getCategory());

        holder.qty.setText(
                "Available: " + p.getQuantityAvailable());

        String imageUrl = p.getImageUrl();

        if (imageUrl == null || imageUrl.trim().isEmpty()) {

            Glide.with(context)
                    .load(R.drawable.ic_home)
                    .into(holder.img);

        } else {

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(holder.img);
        }

        holder.addBtn.setOnClickListener(v -> {

            if (cartListener != null) {
                cartListener.onAddToCart(p);
            }

        });

        // Tapping the card opens the product detail / buy screen.
        holder.itemView.setOnClickListener(v -> {
            if (productClickListener != null) {
                productClickListener.onProductClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null
                ? productList.size()
                : 0;
    }

    public void updateList(List<Product> newList) {

        if (newList != null) {
            this.productList = newList;
            notifyDataSetChanged();
        }
    }

    public static class ProductViewHolder
            extends RecyclerView.ViewHolder {

        ImageView img;
        TextView name, price, category, qty;
        Button addBtn;

        public ProductViewHolder(@NonNull View v) {
            super(v);

            img = v.findViewById(R.id.product_image);
            name = v.findViewById(R.id.product_name);
            price = v.findViewById(R.id.product_price);
            category = v.findViewById(R.id.product_category);
            qty = v.findViewById(R.id.product_quantity);
            addBtn = v.findViewById(R.id.btn_add_to_cart);
        }
    }
}