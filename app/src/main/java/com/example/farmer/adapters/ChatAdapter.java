package com.example.farmer.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Renders chat messages with left-aligned (other user) and
 * right-aligned (current user) bubbles.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final Context context;
    private final long currentUserId;
    private final List<ChatMessage> messages = new ArrayList<>();

    public ChatAdapter(Context context, long currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
    }

    public void updateList(List<ChatMessage> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        boolean isMine = msg.getSenderId() != null && msg.getSenderId().equals(currentUserId);

        holder.bind(msg, isMine);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;
        private final TextView tvTime;
        private final View bubbleContainer;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.chat_message_text);
            tvTime = itemView.findViewById(R.id.chat_message_time);
            bubbleContainer = itemView.findViewById(R.id.chat_bubble_container);
        }

        void bind(ChatMessage msg, boolean isMine) {
            tvMessage.setText(msg.getMessage() != null ? msg.getMessage() : "");

            // Format timestamp
            String timeStr = formatTime(msg.getCreatedAt());
            tvTime.setText(timeStr);

            // Align bubble: mine = end (right), theirs = start (left)
            ViewGroup.LayoutParams params = bubbleContainer.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).gravity = isMine ? Gravity.END : Gravity.START;
            }

            if (isMine) {
                bubbleContainer.setBackgroundResource(R.drawable.bg_chat_bubble_mine);
            } else {
                bubbleContainer.setBackgroundResource(R.drawable.bg_chat_bubble_other);
            }
        }
    }

    private String formatTime(String iso) {
        if (iso == null || iso.isEmpty())
            return "";
        try {
            // Try parsing ISO format: 2026-09-28T10:30:00
            String cleaned = iso.replace('T', ' ').trim();
            if (cleaned.length() >= 16) {
                cleaned = cleaned.substring(0, 16);
            }
            return cleaned;
        } catch (Exception e) {
            return iso;
        }
    }
}