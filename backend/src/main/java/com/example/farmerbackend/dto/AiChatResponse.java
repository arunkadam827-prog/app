package com.example.farmerbackend.dto;

import java.util.List;

/**
 * Response body for {@code POST /api/ai/chat}.
 */
public class AiChatResponse {

    private boolean success;
    private String reply;

    /**
     * Which engine produced the reply:
     * {@code "ai"} when a live LLM answered, {@code "offline"} when the built-in
     * knowledge-base fallback was used.
     */
    private String provider;

    /** Optional follow-up prompts rendered as chips in the app. */
    private List<String> suggestions;

    public AiChatResponse() {
    }

    public AiChatResponse(boolean success, String reply, String provider, List<String> suggestions) {
        this.success = success;
        this.reply = reply;
        this.provider = provider;
        this.suggestions = suggestions;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
