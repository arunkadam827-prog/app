package com.example.farmer.dto;

/**
 * A single turn in the AI conversation history sent to the backend.
 * {@code role} is "user" or "assistant".
 */
public class ChatTurn {

    public String role;
    public String content;

    public ChatTurn() {
    }

    public ChatTurn(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
