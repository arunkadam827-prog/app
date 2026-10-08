package com.example.farmer.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.adapters.ChatAdapter;
import com.example.farmer.models.ChatMessage;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * In-app chat between a buyer and a farmer.
 *
 * <p>
 * Opened from the product detail screen (buyer → farmer) or from the
 * farmer dashboard (farmer → buyer). Messages are persisted on the backend
 * and fetched as a full conversation history.
 * </p>
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_PARTNER_ID = "extra_partner_id";
    public static final String EXTRA_PARTNER_NAME = "extra_partner_name";
    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";

    private long partnerId;
    private String partnerName;
    private String productName;

    private SessionManager sessionManager;
    private ChatAdapter adapter;
    private RecyclerView recycler;
    private View emptyState;
    private EditText input;
    private TextView tvPartnerName, tvPartnerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);

        partnerId = getIntent().getLongExtra(EXTRA_PARTNER_ID, -1);
        partnerName = getIntent().getStringExtra(EXTRA_PARTNER_NAME);
        productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);

        if (partnerId <= 0) {
            Toast.makeText(this, "Cannot start chat: missing partner", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadConversation();
    }

    private void initViews() {
        tvPartnerName = findViewById(R.id.chat_partner_name);
        tvPartnerInfo = findViewById(R.id.chat_partner_info);
        recycler = findViewById(R.id.chat_recycler);
        emptyState = findViewById(R.id.chat_empty);
        input = findViewById(R.id.chat_input);

        tvPartnerName.setText(!TextUtils.isEmpty(partnerName) ? partnerName : "User");
        if (!TextUtils.isEmpty(productName)) {
            tvPartnerInfo.setText(getString(R.string.chat_partner_info_product, productName));
        } else {
            tvPartnerInfo.setText(R.string.chat_partner_info_default);
        }

        findViewById(R.id.chat_back).setOnClickListener(v -> finish());

        long currentUserId = sessionManager.getUserId();
        adapter = new ChatAdapter(this, currentUserId);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // Send on button click
        findViewById(R.id.chat_send_btn).setOnClickListener(v -> sendMessage());

        // Send on Enter (actionSend)
        input.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadConversation() {
        long currentUserId = sessionManager.getUserId();
        if (currentUserId <= 0) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RetrofitClient.getApiService()
                .getConversation(currentUserId, partnerId)
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ChatMessage>> call,
                            @NonNull Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ChatMessage> messages = response.body();
                            adapter.updateList(messages);
                            if (messages.isEmpty()) {
                                emptyState.setVisibility(View.VISIBLE);
                                recycler.setVisibility(View.GONE);
                            } else {
                                emptyState.setVisibility(View.GONE);
                                recycler.setVisibility(View.VISIBLE);
                                recycler.scrollToPosition(messages.size() - 1);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ChatMessage>> call,
                            @NonNull Throwable t) {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                });

        // Mark incoming messages as read
        RetrofitClient.getApiService()
                .markConversationRead(currentUserId, partnerId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call,
                            @NonNull Response<Map<String, Object>> response) {
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call,
                            @NonNull Throwable t) {
                    }
                });
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        long currentUserId = sessionManager.getUserId();
        if (currentUserId <= 0) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage msg = new ChatMessage(currentUserId, partnerId, text);
        if (!TextUtils.isEmpty(productName)) {
            msg.setProductName(productName);
        }

        // Disable input while sending
        input.setEnabled(false);
        findViewById(R.id.chat_send_btn).setEnabled(false);

        RetrofitClient.getApiService()
                .sendMessage(msg)
                .enqueue(new Callback<ChatMessage>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatMessage> call,
                            @NonNull Response<ChatMessage> response) {
                        input.setEnabled(true);
                        findViewById(R.id.chat_send_btn).setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            input.setText("");
                            adapter.addMessage(response.body());
                            emptyState.setVisibility(View.GONE);
                            recycler.setVisibility(View.VISIBLE);
                            recycler.scrollToPosition(adapter.getItemCount() - 1);
                        } else {
                            Toast.makeText(ChatActivity.this,
                                    "Failed to send message", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatMessage> call,
                            @NonNull Throwable t) {
                        input.setEnabled(true);
                        findViewById(R.id.chat_send_btn).setEnabled(true);
                        Toast.makeText(ChatActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}