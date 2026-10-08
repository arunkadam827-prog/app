package com.example.farmerbackend.dto;

import java.util.List;

/**
 * Request body for {@code POST /api/ai/chat}.
 */
public class AiChatRequest {

    /** The authenticated user id (may be null for anonymous sessions). */
    private Long userId;

    /** BUYER or FARMER — lets the assistant tailor its answers. */
    private String userType;

    /** The message the user just typed. */
    private String message;

    /** Previous turns (oldest first) so the assistant keeps context. */
    private List<ChatTurn> history;

    public AiChatRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatTurn> getHistory() {
        return history;
    }

    public void setHistory(List<ChatTurn> history) {
        this.history = history;
    }
}
