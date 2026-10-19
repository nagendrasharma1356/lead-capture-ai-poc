package com.hexawarre.leadpoc.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexawarre.leadpoc.model.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ClaudeQualifier implements LeadQualifier {

    private static final Logger log = LoggerFactory.getLogger(ClaudeQualifier.class);

    private final RuleBasedQualifier fallback;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestClient client = RestClient.create();
    private final String apiKey;
    private final String model;

    public ClaudeQualifier(RuleBasedQualifier fallback,
                           @Value("${app.ai.anthropic.api-key}") String apiKey,
                           @Value("${app.ai.anthropic.model}") String model) {
        this.fallback = fallback;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public Qualification qualify(Lead lead) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback.qualify(lead);
        }
        try {
            String prompt = """
                    You qualify inbound property leads for a sales team. Reply with ONLY a JSON object:
                    {"score": 0-100, "summary": "one sentence", "tags": ["short","tags"]}
                    Lead: property type=%s, condition=%s, zip=%s, address=%s, has email=%s.
                    """.formatted(lead.getPropertyType(), lead.getPropertyCondition(), lead.getZip(),
                    lead.getPropertyAddress(), lead.getEmail() != null && !lead.getEmail().isBlank());

            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", 300,
                    "messages", List.of(Map.of("role", "user", "content", prompt)));

            String response = client.post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String text = mapper.readTree(response).path("content").path(0).path("text").asText();
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            JsonNode json = mapper.readTree(text.substring(start, end + 1));

            int score = Math.max(0, Math.min(100, json.path("score").asInt(50)));
            StringBuilder tags = new StringBuilder();
            for (JsonNode t : json.path("tags")) {
                if (tags.length() > 0) tags.append(',');
                tags.append(t.asText());
            }
            return new Qualification(score, Qualification.tierFor(score),
                    json.path("summary").asText(""), tags.toString(), "claude");
        } catch (Exception e) {
            log.warn("Claude qualification failed, using rule-based fallback: {}", e.getMessage());
            return fallback.qualify(lead);
        }
    }
}
