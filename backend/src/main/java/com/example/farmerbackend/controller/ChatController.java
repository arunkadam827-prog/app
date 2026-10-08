package com.example.farmerbackend.controller;

import com.example.farmerbackend.entity.ChatMessage;
import com.example.farmerbackend.repository.ChatMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatMessageRepository chatRepository;

    public ChatController(ChatMessageRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessage message) {
        try {
            ChatMessage saved = chatRepository.save(message);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<?> getConversation(@PathVariable Long userId1,
                                              @PathVariable Long userId2) {
        List<ChatMessage> messages = chatRepository.findConversation(userId1, userId2);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getRecentConversations(@PathVariable Long userId) {
        List<ChatMessage> recent = chatRepository.findRecentConversations(userId);
        return ResponseEntity.ok(recent);
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId) {
        long count = chatRepository.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/read/{userId}/{otherUserId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long userId,
                                         @PathVariable Long otherUserId) {
        chatRepository.markConversationAsRead(userId, otherUserId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}