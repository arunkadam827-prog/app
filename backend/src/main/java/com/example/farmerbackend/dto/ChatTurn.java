package com.example.farmerbackend.dto;

/**
 * A single turn in an AI chat conversation.
 *
 * <p>
 * {@code role} is either {@code "user"} or {@code "assistant"} — matching the
 * OpenAI-style chat message schema so the history can be forwarded to the LLM
 * without any transformation.
 * </p>
 */
public class ChatTurn {

    private String role;
    private String content;

    public ChatTurn() {
    }

    public ChatTurn(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
