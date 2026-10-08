package com.example.farmer.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Response body for POST /api/ai/chat. */
public class AiChatResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("reply")
    public String reply;

    /** "ai" for a live LLM answer, "offline" for the knowledge-base fallback. */
    @SerializedName("provider")
    public String provider;

    @SerializedName("suggestions")
    public List<String> suggestions;

    public AiChatResponse() {
    }
}
