package com.example.farmer.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.models.Lead;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the list of buyer inquiries (leads) on the farmer dashboard.
 *
 * <p>
 * Each row shows the buyer's name, the product & quantity they are
 * interested in, any note they attached, and two direct-contact actions:
 * Call and WhatsApp chat. The WhatsApp conversation is handled end-to-end
 * by the WhatsApp client, so no message content ever passes through the app.
 * </p>
 */
public class LeadAdapter extends RecyclerView.Adapter<LeadAdapter.LeadViewHolder> {

    /** Callbacks for the direct-contact actions exposed on each lead row. */
    public interface OnLeadActionListener {
        void onCallClick(Lead lead);

        void onChatClick(Lead lead);

        /** Opens the in-app chat screen for a threaded conversation with the buyer. */
        void onInAppChatClick(Lead lead);
    }

    private final Context context;
    private final OnLeadActionListener listener;
    private final List<Lead> leads = new ArrayList<>();

    public LeadAdapter(Context context, OnLeadActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /** Replaces the currently displayed leads with a freshly fetched list. */
    public void updateList(List<Lead> newLeads) {
        leads.clear();
        if (newLeads != null) {
            leads.addAll(newLeads);
        }
        notifyDataSetChanged();
    }

    /** Number of leads currently held by the adapter. */
    public int getLeadCount() {
        return leads.size();
    }

    @NonNull
    @Override
    public LeadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lead, parent, false);
        return new LeadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeadViewHolder holder, int position) {
        holder.bind(leads.get(position));
    }

    @Override
    public int getItemCount() {
        return leads.size();
    }

    class LeadViewHolder extends RecyclerView.ViewHolder {

        private final TextView buyerName;
        private final TextView buyerContact;
        private final TextView date;
        private final TextView statusBadge;
        private final TextView productName;
        private final TextView quantity;
        private final TextView message;
        private final MaterialButton callButton;
        private final MaterialButton inAppChatButton;
        private final MaterialButton chatButton;

        LeadViewHolder(@NonNull View itemView) {
            super(itemView);
            buyerName = itemView.findViewById(R.id.lead_buyer_name);
            buyerContact = itemView.findViewById(R.id.lead_buyer_contact);
            date = itemView.findViewById(R.id.lead_date);
            statusBadge = itemView.findViewById(R.id.lead_status_badge);
            productName = itemView.findViewById(R.id.lead_product_name);
            quantity = itemView.findViewById(R.id.lead_quantity);
            message = itemView.findViewById(R.id.lead_message);
            callButton = itemView.findViewById(R.id.lead_call_button);
            inAppChatButton = itemView.findViewById(R.id.lead_chat_button);
            chatButton = itemView.findViewById(R.id.lead_whatsapp_button);
        }

        void bind(Lead lead) {
            buyerName.setText(!TextUtils.isEmpty(lead.getBuyerName())
                    ? lead.getBuyerName()
                    : context.getString(R.string.lead_interested_buyer));

            if (TextUtils.isEmpty(lead.getBuyerPhone())) {
                buyerContact.setVisibility(View.GONE);
            } else {
                buyerContact.setVisibility(View.VISIBLE);
                buyerContact.setText(lead.getBuyerPhone());
            }

            productName.setText(!TextUtils.isEmpty(lead.getProductName())
                    ? lead.getProductName()
                    : context.getString(R.string.lead_your_produce));

            quantity.setText(!TextUtils.isEmpty(lead.getQuantity())
                    ? lead.getQuantity()
                    : context.getString(R.string.lead_qty_not_specified));

            date.setText(formatDate(lead.getCreatedAt()));

            if (TextUtils.isEmpty(lead.getMessage())) {
                message.setVisibility(View.GONE);
            } else {
                message.setVisibility(View.VISIBLE);
                message.setText(lead.getMessage());
            }

            applyStatusBadge(statusBadge, lead.getStatus());

            callButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallClick(lead);
                }
            });

            inAppChatButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInAppChatClick(lead);
                }
            });

            chatButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(lead);
                }
            });
        }

        /**
         * Colours the status pill to match the lead workflow:
         * NEW (amber) → CONTACTED (green) → CLOSED (muted).
         */
        private void applyStatusBadge(TextView badge, String rawStatus) {
            String status = TextUtils.isEmpty(rawStatus) ? "NEW" : rawStatus.trim().toUpperCase();

            switch (status) {
                case "CONTACTED":
                    badge.setText(R.string.lead_status_contacted);
                    badge.setBackgroundResource(R.drawable.bg_pill_primary);
                    badge.setTextColor(ContextCompat.getColor(context, R.color.primary_dark));
                    break;
                case "CLOSED":
                    badge.setText(R.string.lead_status_closed);
                    badge.setBackgroundResource(R.drawable.bg_status_badge);
                    badge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    break;
                default:
                    badge.setText(R.string.lead_status_new);
                    badge.setBackgroundResource(R.drawable.bg_pill_accent);
                    badge.setTextColor(ContextCompat.getColor(context, R.color.accent_dark));
                    break;
            }
        }
    }

    /** Trims an ISO-8601 timestamp down to something friendly for a list row. */
    private String formatDate(String createdAt) {
        if (TextUtils.isEmpty(createdAt)) {
            return context.getString(R.string.lead_recent_inquiry);
        }
        String cleaned = createdAt.replace('T', ' ').trim();
        if (cleaned.length() > 16) {
            cleaned = cleaned.substring(0, 16);
        }
        return context.getString(R.string.lead_enquired_on, cleaned);
    }
}
