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
import com.example.farmer.models.AiMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Renders Kisan AI chat: user turns on the right (green), AI turns on the left
 * (white card with the assistant avatar).
 */
public class AiChatAdapter extends RecyclerView.Adapter<AiChatAdapter.VH> {

    private final Context context;
    private final List<AiMessage> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public AiChatAdapter(Context context) {
        this.context = context;
    }

    public void updateList(List<AiMessage> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(AiMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /** Replaces the last message (used to swap a typing indicator for the reply). */
    public void replaceLast(AiMessage message) {
        if (messages.isEmpty()) {
            addMessage(message);
            return;
        }
        messages.set(messages.size() - 1, message);
        notifyItemChanged(messages.size() - 1);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_message, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class VH extends RecyclerView.ViewHolder {

        private final TextView tvMessage;
        private final TextView tvTime;
        private final View bubbleContainer;
        private final View avatar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.ai_message_text);
            tvTime = itemView.findViewById(R.id.ai_message_time);
            bubbleContainer = itemView.findViewById(R.id.ai_bubble_container);
            avatar = itemView.findViewById(R.id.ai_avatar);
        }

        void bind(AiMessage msg) {
            boolean isUser = msg.sender == AiMessage.SENDER_USER;

            tvMessage.setText(msg.text != null ? msg.text : "");
            avatar.setVisibility(isUser ? View.GONE : View.VISIBLE);

            // Bouncing dots while the AI reply is pending.
            tvTime.setText(msg.pending ? context.getString(R.string.ai_typing) : timeFormat.format(new Date()));

            ViewGroup.LayoutParams params = bubbleContainer.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).gravity =
                        isUser ? Gravity.END : Gravity.START;
            }

            bubbleContainer.setBackgroundResource(
                    isUser ? R.drawable.bg_chat_bubble_mine : R.drawable.bg_chat_bubble_ai);
        }
    }
}
