package com.example.farmer.dto;

import java.util.List;

/** Request body for POST /api/ai/chat. */
public class AiChatRequest {

    public Long userId;
    public String userType;
    public String message;
    public List<ChatTurn> history;

    public AiChatRequest() {
    }

    public AiChatRequest(Long userId, String userType, String message, List<ChatTurn> history) {
        this.userId = userId;
        this.userType = userType;
        this.message = message;
        this.history = history;
    }
}
