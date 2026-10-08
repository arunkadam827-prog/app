package com.example.farmer.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmer.R;
import com.example.farmer.adapters.AiChatAdapter;
import com.example.farmer.dto.AiChatRequest;
import com.example.farmer.dto.AiChatResponse;
import com.example.farmer.dto.ChatTurn;
import com.example.farmer.models.AiMessage;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Kisan AI — AI-powered customer support / chatbot screen.
 *
 * <p>
 * Sends the user's message plus the recent conversation history to the backend
 * ({@code POST /api/ai/chat}), which enriches the request with live marketplace
 * data before calling the language model. See the backend {@code AiService}.
 * </p>
 */
public class AiChatActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AiChatAdapter adapter;
    private RecyclerView recycler;
    private EditText input;
    private MaterialButton sendButton;
    private LinearLayout suggestionsContainer;
    private View suggestionsScroll;
    private TextView tvStatus;

    /** Conversation history forwarded to the model for context. */
    private final List<ChatTurn> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        sessionManager = new SessionManager(this);
        initViews();
        startConversation();
    }

    private void initViews() {
        recycler = findViewById(R.id.ai_recycler);
        input = findViewById(R.id.ai_input);
        sendButton = findViewById(R.id.ai_send_btn);
        suggestionsContainer = findViewById(R.id.ai_suggestions);
        suggestionsScroll = findViewById(R.id.ai_suggestions_scroll);
        tvStatus = findViewById(R.id.ai_status);

        adapter = new AiChatAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.ai_back).setOnClickListener(v -> finish());
        findViewById(R.id.ai_clear).setOnClickListener(v -> {
            history.clear();
            adapter.updateList(new ArrayList<>());
            startConversation();
        });

        sendButton.setOnClickListener(v -> sendMessage());
        input.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    /** Greets the user and shows a first set of suggestion chips. */
    private void startConversation() {
        String greeting = getString(R.string.ai_greeting);
        adapter.addMessage(new AiMessage(AiMessage.SENDER_AI, greeting));
        history.add(new ChatTurn("assistant", greeting));
        showSuggestions(Arrays.asList(
                getString(R.string.ai_suggestion_products),
                getString(R.string.ai_suggestion_order),
                getString(R.string.ai_suggestion_payment),
                getString(R.string.ai_suggestion_support)));
        scrollToBottom();
    }

    private void sendMessage() {
        sendMessage(input.getText().toString().trim());
    }

    private void sendMessage(String rawText) {
        final String text = rawText == null ? "" : rawText.trim();
        if (text.isEmpty()) {
            return;
        }

        input.setText("");
        adapter.addMessage(new AiMessage(AiMessage.SENDER_USER, text));
        history.add(new ChatTurn("user", text));
        scrollToBottom();
        hideSuggestions();

        // Show a typing indicator bubble that the reply will replace.
        adapter.addMessage(new AiMessage(AiMessage.SENDER_AI, "", true));
        scrollToBottom();

        setBusy(true);
        tvStatus.setText(R.string.ai_status_thinking);

        Long userId = sessionManager.getUserId();
        String userType = sessionManager.getUserType();
        AiChatRequest request = new AiChatRequest(
                userId != null && userId > 0 ? userId : null,
                userType,
                text,
                new ArrayList<>(history));

        RetrofitClient.getApiService()
                .aiChat(request)
                .enqueue(new Callback<AiChatResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AiChatResponse> call,
                            @NonNull Response<AiChatResponse> response) {
                        setBusy(false);
                        tvStatus.setText(R.string.ai_subtitle);

                        String reply = null;
                        List<String> suggestions = null;
                        if (response.isSuccessful() && response.body() != null
                                && !TextUtils.isEmpty(response.body().reply)) {
                            reply = response.body().reply;
                            suggestions = response.body().suggestions;
                        }

                        if (reply == null) {
                            reply = getString(R.string.ai_error);
                            finishTurn(reply, null);
                        } else {
                            finishTurn(reply, suggestions);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AiChatResponse> call, @NonNull Throwable t) {
                        setBusy(false);
                        tvStatus.setText(R.string.ai_subtitle);
                        finishTurn(getString(R.string.ai_network_error), null);
                    }
                });
    }

    /** Replaces the pending bubble with the final reply and updates suggestions. */
    private void finishTurn(String reply, List<String> suggestions) {
        adapter.replaceLast(new AiMessage(AiMessage.SENDER_AI, reply));
        history.add(new ChatTurn("assistant", reply));
        scrollToBottom();

        if (suggestions != null && !suggestions.isEmpty()) {
            showSuggestions(suggestions);
        }
    }

    private void showSuggestions(List<String> suggestions) {
        suggestionsContainer.removeAllViews();
        if (suggestions == null || suggestions.isEmpty()) {
            hideSuggestions();
            return;
        }

        int padH = (int) getResources().getDimension(R.dimen.space_md);
        int padV = (int) getResources().getDimension(R.dimen.space_sm);
        int gap = (int) getResources().getDimension(R.dimen.space_sm);

        for (String suggestion : suggestions) {
            TextView chip = new TextView(this);
            chip.setText(suggestion);
            chip.setTextSize(13f);
            chip.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            chip.setBackgroundResource(R.drawable.bg_suggestion_chip);
            chip.setPadding(padH, padV, padH, padV);
            chip.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(gap);
            chip.setLayoutParams(lp);
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> sendMessage(suggestion));

            suggestionsContainer.addView(chip);
        }
        suggestionsScroll.setVisibility(View.VISIBLE);
    }

    private void hideSuggestions() {
        suggestionsContainer.removeAllViews();
        suggestionsScroll.setVisibility(View.GONE);
    }

    private void setBusy(boolean busy) {
        sendButton.setEnabled(!busy);
        input.setEnabled(!busy);
    }

    private void scrollToBottom() {
        recycler.post(() -> {
            if (adapter.getItemCount() > 0) {
                recycler.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }
}
