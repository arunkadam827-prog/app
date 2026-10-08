package com.example.farmerbackend.controller;

import com.example.farmerbackend.dto.AiChatRequest;
import com.example.farmerbackend.dto.AiChatResponse;
import com.example.farmerbackend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI customer-support / chatbot endpoint.
 *
 * <p>
 * Powers the in-app "Kisan AI" assistant. Delegates all reasoning and
 * fail-over handling to {@link AiService}.
 * </p>
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AiChatRequest request) {
        try {
            AiChatResponse response = aiService.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
