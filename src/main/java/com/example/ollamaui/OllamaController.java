package com.example.ollamaui;

import com.squareup.okhttp.OkHttpClient;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class OllamaController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final OllamaAPI ollamaAPI;
    private final Map<String, Session> sessions = new HashMap<>();

    @Autowired
    private SimpMessagingTemplate template;

    public OllamaController() {
        OkHttpClient client = new OkHttpClient()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
        this.ollamaAPI = new OllamaAPI("http://localhost:11434", client);
    }

    // Create new session with selected model
    // Modify session creation to broadcast properly
    @MessageMapping("/create-session")
    @SendTo("/topic/sessions")
    public SessionResponse createSession(SessionRequest request) {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, request.model());
        log.info("Created session: {} with model: {}", sessionId, request.model());
        sessions.put(sessionId, session);
        return new SessionResponse(
                sessionId,
                request.model(),
                session.getCreatedAt()
        );
    }

    @GetMapping("/session-history")
    public List<OllamaChatMessage> getSessionHistory(@RequestParam String sessionId) {
        Session session = sessions.get(sessionId);
        return session != null ? session.getHistory() : Collections.emptyList();
    }



    @MessageMapping("/submit-prompt")
    @SendTo("/topic/responses")
    public Response handlePrompt(PromptRequest request) throws Exception {

        log.info("Prompt: {}" , request.prompt());

        try {
            Session session = sessions.get(request.sessionId());
            if (session == null) {
                log.warn("Invalid session access: {}", request.sessionId());
                return new Response(request.sessionId(), "Session expired or invalid");
            }
            // Build chat request using SDK
            OllamaChatRequestBuilder chatRequestBuilder =  OllamaChatRequestBuilder.getInstance(session.model);

            chatRequestBuilder.withMessage(
                            OllamaChatMessageRole.USER,
                            request.prompt()
            );

            // Execute request and extract clean response
            String responseText = ollamaAPI.chat(chatRequestBuilder.build()).getResponse();

            log.info("Response: {}" , responseText);

            // Maintain session history
            session.addMessage(OllamaChatMessageRole.USER, request.prompt().toString());
            session.addMessage(OllamaChatMessageRole.ASSISTANT, responseText);

            return new Response(request.sessionId(), responseText);

        } catch (Exception e) {
            log.error("Error processing prompt: {}", e.getMessage());
            return new Response(request.sessionId(), "Error: " + e.getMessage());
        }
    }



    // Rest of the code remains same as previous version
    // (createSession, getHistory, suggestModel, etc.)

    record SessionRequest(String model) {}
    record SessionResponse(String sessionId, String model, Instant createdAt) {}
    record PromptRequest(String sessionId, String prompt) {}
    record Response(String sessionId, String content) {}

    class Session {

        private final String sessionId;
        private final String model;
        private final List<OllamaChatMessage> history = new ArrayList<>();
        private final Instant createdAt;

        public Session(String sessionId, String model) {
            this.sessionId = sessionId;
            this.model = model;
            this.createdAt = Instant.now();
        }

        public Instant getCreatedAt() {
            return createdAt;
        }



        public void addMessage(OllamaChatMessageRole role, String content) {
            history.add(new OllamaChatMessage(role, content));
        }

        public List<OllamaChatMessage> getHistory() {
            return new LinkedList<>(history);
        }

        public String model() {
            return model;
        }
    }
}