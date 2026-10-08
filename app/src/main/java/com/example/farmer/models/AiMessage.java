package com.example.farmer.models;

/**
 * UI model for a single bubble in the Kisan AI assistant chat.
 * {@code sender} is {@link #SENDER_USER} or {@link #SENDER_AI}.
 */
public class AiMessage {

    public static final int SENDER_USER = 0;
    public static final int SENDER_AI = 1;

    public final int sender;
    public String text;
    /** True while waiting for the AI reply (shows a typing indicator). */
    public boolean pending;

    public AiMessage(int sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public AiMessage(int sender, String text, boolean pending) {
        this.sender = sender;
        this.text = text;
        this.pending = pending;
    }
}
