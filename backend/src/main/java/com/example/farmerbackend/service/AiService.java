package com.example.farmerbackend.service;

import com.example.farmerbackend.dto.AiChatRequest;
import com.example.farmerbackend.dto.AiChatResponse;
import com.example.farmerbackend.dto.ChatTurn;
import com.example.farmerbackend.entity.CustomerOrder;
import com.example.farmerbackend.entity.Product;
import com.example.farmerbackend.repository.CustomerOrderRepository;
import com.example.farmerbackend.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Real AI customer-support / chatbot engine for the Farmer+ marketplace.
 *
 * <p>
 * The service is <b>context aware</b>: before calling the large language model it
 * assembles a live snapshot of the marketplace (featured products, the signed-in
 * user's recent orders) and injects it into the model's system prompt. This is
 * what turns a generic chatbot into a genuinely useful support assistant that can
 * answer "what do you sell?" or "where is my order?" with real data.
 * </p>
 *
 * <p>
 * Two provider families are supported:
 * </p>
 * <ul>
 * <li>{@code openai} — any OpenAI-compatible Chat Completions endpoint
 * (OpenAI, Groq, Together, OpenRouter, a local Ollama/LM Studio server…).</li>
 * <li>{@code gemini} — Google's Generative Language API.</li>
 * </ul>
 *
 * <p>
 * If no API key is configured, or the upstream call fails, the service degrades
 * gracefully to a deterministic, rule-based knowledge base ({@code provider =
 * "offline"}) so the customer-support screen always answers.
 * </p>
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Configuration (see application.properties) ──
    @Value("${ai.enabled:true}")
    private boolean enabled;

    @Value("${ai.provider:openai}")
    private String provider;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.timeout-seconds:25}")
    private int timeoutSeconds;

    @Value("${ai.max-context-products:8}")
    private int maxContextProducts;

    public AiService(ProductRepository productRepository,
                     CustomerOrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // ─────────────────────────────────────────────────────────────
    //  Public entry point
    // ─────────────────────────────────────────────────────────────

    public AiChatResponse chat(AiChatRequest request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        if (message.isEmpty()) {
            return new AiChatResponse(true,
                    "Namaste! 🌾 I'm Kisan AI, your Farming Assistant. Ask me about products, orders, delivery or selling on Farmer+.",
                    "offline",
                    defaultSuggestions(request.getUserType()));
        }

        String context = buildMarketplaceContext(request.getUserId(), request.getUserType());

        // 1) Try the live LLM when configured.
        if (enabled && apiKey != null && !apiKey.isBlank()) {
            try {
                String reply = "gemini".equalsIgnoreCase(provider)
                        ? callGemini(buildSystemPrompt(request.getUserType(), context), request.getHistory(), message)
                        : callOpenAiCompatible(buildSystemPrompt(request.getUserType(), context), request.getHistory(), message);

                if (reply != null && !reply.isBlank()) {
                    return new AiChatResponse(true, reply.trim(), "ai",
                            suggestionsFor(message, request.getUserType()));
                }
            } catch (Exception e) {
                log.warn("AI provider '{}' call failed, falling back to offline knowledge base: {}",
                        provider, e.getMessage());
            }
        }

        // 2) Graceful fallback — never leave the user without an answer.
        String reply = fallbackReply(message, request.getUserType(), context);
        return new AiChatResponse(true, reply, "offline",
                suggestionsFor(message, request.getUserType()));
    }

    // ─────────────────────────────────────────────────────────────
    //  Marketplace context (RAG-lite: real data injected into prompt)
    // ─────────────────────────────────────────────────────────────

    private String buildMarketplaceContext(Long userId, String userType) {
        StringBuilder sb = new StringBuilder();

        try {
            List<Product> products = productRepository.findAllByOrderByProductIdDesc();
            if (!products.isEmpty()) {
                sb.append("LIVE MARKETPLACE CATALOGUE (most recent first):\n");
                int limit = Math.min(products.size(), Math.max(1, maxContextProducts));
                for (int i = 0; i < limit; i++) {
                    Product p = products.get(i);
                    sb.append("- ").append(p.getProductName())
                            .append(" | ₹").append(formatPrice(p.getPrice()))
                            .append(" | category: ").append(safe(p.getCategory(), "General"))
                            .append(" | stock: ").append(p.getQuantityAvailable())
                            .append(" | farmer: ").append(p.getFarmerName())
                            .append(p.getFarmerCity() != null ? " (" + p.getFarmerCity() + ")" : "")
                            .append("\n");
                }
                sb.append("Total products listed: ").append(products.size()).append("\n");
            } else {
                sb.append("The marketplace currently has no products listed.\n");
            }

            if (userId != null && userId > 0) {
                List<CustomerOrder> orders = orderRepository.findByUserUserIdOrderByOrderIdDesc(userId);
                if (orders != null && !orders.isEmpty()) {
                    sb.append("\nTHIS USER'S RECENT ORDERS:\n");
                    int limit = Math.min(orders.size(), 5);
                    for (int i = 0; i < limit; i++) {
                        CustomerOrder o = orders.get(i);
                        sb.append("- Order #").append(o.getOrderId())
                                .append(" | ").append(o.getProductName())
                                .append(" | ₹").append(formatPrice(o.getTotalAmount()))
                                .append(" | status: ").append(safe(o.getStatus(), "PLACED"))
                                .append("\n");
                    }
                } else {
                    sb.append("\nThis user has no orders yet.\n");
                }
            }

            sb.append("\nSu" + "pport: support@farmerapp.com | Call/WhatsApp 9356601104 | "
                    + "Working hours Mon-Sat 9AM-6PM IST.\n");
        } catch (Exception e) {
            log.warn("Could not build marketplace context: {}", e.getMessage());
        }

        return sb.toString();
    }

    private String buildSystemPrompt(String userType, String context) {
        String role = "FARMER".equalsIgnoreCase(userType) ? "farmer/seller" : "buyer";
        return "You are Kisan AI, the friendly and knowledgeable customer-support assistant "
                + "embedded in the Farmer+ app — an Indian farm-to-consumer marketplace that connects "
                + "local farmers directly with buyers.\n\n"
                + "The person you are helping is signed in as a " + role + ".\n\n"
                + "GUIDELINES:\n"
                + "1. Be warm, concise and practical. Use simple English.\n"
                + "2. Prefer 2-5 short sentences or a short bullet list. Never write long essays.\n"
                + "3. Use ONLY the live data below when mentioning products, prices, stock or orders. "
                + "Never invent product names, prices or order statuses.\n"
                + "4. If the requested data is not in the context, say you could not find it and point "
                + "the user to Support (support@farmerapp.com / 9356601104).\n"
                + "5. Help with: finding produce, pricing guidance, order tracking, delivery, returns, "
                + "payment methods (UPI, card, bank transfer, COD), and how to sell as a farmer.\n"
                + "6. If asked something unrelated to farming or the marketplace, politely redirect.\n"
                + "7. You may use light emojis (🌱🚚💳) sparingly.\n\n"
                + "=== LIVE DATA ===\n" + context + "=== END LIVE DATA ===";
    }

    // ─────────────────────────────────────────────────────────────
    //  Provider: OpenAI-compatible Chat Completions
    // ─────────────────────────────────────────────────────────────

    private String callOpenAiCompatible(String systemPrompt, List<ChatTurn> history, String userMessage)
            throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (history != null) {
            int start = Math.max(0, history.size() - 8); // keep the last 8 turns
            for (int i = start; i < history.size(); i++) {
                ChatTurn t = history.get(i);
                if (t == null || t.getContent() == null || t.getContent().isBlank()) {
                    continue;
                }
                String r = "assistant".equalsIgnoreCase(t.getRole()) ? "assistant" : "user";
                messages.add(Map.of("role", r, "content", t.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.4);
        body.put("max_tokens", 600);

        String url = baseUrl.endsWith("/")
                ? baseUrl + "chat/completions"
                : baseUrl + "/chat/completions";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("OpenAI-compatible API returned HTTP "
                    + response.statusCode() + ": " + truncate(response.body(), 300));
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        return content.isMissingNode() ? null : content.asText();
    }

    // ─────────────────────────────────────────────────────────────
    //  Provider: Google Gemini
    // ─────────────────────────────────────────────────────────────

    private String callGemini(String systemPrompt, List<ChatTurn> history, String userMessage)
            throws Exception {
        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                ChatTurn t = history.get(i);
                if (t == null || t.getContent() == null || t.getContent().isBlank()) {
                    continue;
                }
                String role = "assistant".equalsIgnoreCase(t.getRole()) ? "model" : "user";
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", t.getContent()))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", userMessage))));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        body.put("contents", contents);
        body.put("generationConfig", Map.of("temperature", 0.4, "maxOutputTokens", 600));

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Gemini API returned HTTP "
                    + response.statusCode() + ": " + truncate(response.body(), 300));
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        return text.isMissingNode() ? null : text.asText();
    }

    private HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  Offline knowledge base (deterministic fallback)
    // ─────────────────────────────────────────────────────────────

    private String fallbackReply(String message, String userType, String context) {
        String m = message.toLowerCase(Locale.ROOT);

        if (containsAny(m, "hi", "hello", "hey", "namaste", "good morning", "good evening")) {
            return "Namaste! 🌱 I'm Kisan AI, your Farmer+ assistant. I can help you find fresh produce, "
                    + "track an order, understand payments and delivery, or start selling. What would you like to do?";
        }
        if (containsAny(m, "order", "track", "delivery status", "where is my")) {
            return "You can see every order in the Orders tab, including its live status (PLACED, SHIPPED, DELIVERED). "
                    + "Standard delivery usually takes 2–3 business days. If an order looks stuck, share the order number "
                    + "and our team at 9356601104 will follow up.";
        }
        if (containsAny(m, "delivery", "shipping", "how long", "when will")) {
            return "🚚 Delivery is typically 2–3 business days after dispatch, and it is free on most orders. "
                    + "You'll get a status update in the Orders tab as the order moves along.";
        }
        if (containsAny(m, "pay", "payment", "upi", "cod", "card", "refund")) {
            return "💳 We accept UPI (GPay/PhonePe/Paytm), debit & credit cards, direct bank transfer and Cash on Delivery. "
                    + "Payments are collected securely at checkout. For a refund or a payment issue, email support@farmerapp.com.";
        }
        if (containsAny(m, "return", "refund", "damaged", "quality", "stale")) {
            return "If produce arrives damaged or is not fresh, please raise it within 24 hours with a photo. "
                    + "We arrange a replacement or refund after a quick check. Contact support@farmerapp.com or call 9356601104.";
        }
        if (containsAny(m, "sell", "add product", "list", "farmer dashboard")) {
            return "As a farmer you can add produce from the Farmer Dashboard → Add product. Set a clear name, fair price, "
                    + "available quantity and a photo. Buyers can then message you directly to negotiate.";
        }
        if (containsAny(m, "price", "cost", "rate", "cheap", "discount")) {
            return "Prices are set directly by farmers, so you always get a fair farm-gate rate. "
                    + "Tell me what you're looking for and I'll point you to the best options in the catalogue.";
        }
        if (containsAny(m, "product", "buy", "available", "vegetable", "fruit", "grain", "dairy", "organic", "what do you")) {
            return "Here's what buyers are browsing right now:\n\n" + extractCatalogue(context)
                    + "\nOpen the Home tab to see full details and add items to your cart.";
        }
        if (containsAny(m, "contact", "support", "help", "human", "call", "email")) {
            return "You can reach our support team at support@farmerapp.com or call/WhatsApp 9356601104, "
                    + "Monday–Saturday, 9 AM–6 PM IST. We're happy to help! 🌾";
        }
        if (containsAny(m, "thank", "thanks", "dhanyavad")) {
            return "You're most welcome! 🌱 Happy farming and happy shopping with Farmer+.";
        }

        return "I can help with fresh products, order tracking, payments, delivery, returns and selling on Farmer+. "
                + "Could you tell me a little more about what you need? If it's urgent, reach us at support@farmerapp.com "
                + "or 9356601104.";
    }

    /** Extracts the "- name | ₹price ..." lines from the injected catalogue context. */
    private String extractCatalogue(String context) {
        if (context == null || context.isBlank()) {
            return "No products are listed at the moment.";
        }
        StringBuilder out = new StringBuilder();
        for (String line : context.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- ") && trimmed.contains("₹")) {
                out.append(trimmed).append("\n");
                if (out.toString().split("\n").length >= 5) {
                    break;
                }
            }
        }
        return out.length() == 0 ? "No products are listed at the moment." : out.toString().trim();
    }

    private List<String> suggestionsFor(String message, String userType) {
        String m = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (containsAny(m, "order", "delivery", "track")) {
            return List.of("Delivery charges?", "Return policy", "Talk to support");
        }
        if (containsAny(m, "sell", "product", "farmer")) {
            return List.of("How do I price my crop?", "Payment for farmers", "Talk to support");
        }
        return defaultSuggestions(userType);
    }

    private List<String> defaultSuggestions(String userType) {
        if ("FARMER".equalsIgnoreCase(userType)) {
            return List.of("How do I add a product?", "When do I get paid?", "Talk to support");
        }
        return List.of("What products are available?", "Track my order", "Payment options");
    }

    // ─────────────────────────────────────────────────────────────
    //  Small helpers
    // ─────────────────────────────────────────────────────────────

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price)) {
            return String.valueOf((long) price);
        }
        return String.format(Locale.ROOT, "%.2f", price);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "…";
    }
}
