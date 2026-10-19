package com.hexawarre.leadpoc.service;

import com.hexawarre.leadpoc.model.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/** Sends a new lead to a Zapier / Make.com catch-hook (or any URL). Blank URL = log only. */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final RestClient client = RestClient.create();
    private final String url;

    public WebhookService(@Value("${app.webhook.url}") String url) {
        this.url = url;
    }

    public boolean send(Lead lead) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", lead.getId());
        payload.put("name", lead.getName());
        payload.put("phone", lead.getPhone());
        payload.put("email", lead.getEmail());
        payload.put("zip", lead.getZip());
        payload.put("propertyAddress", lead.getPropertyAddress());
        payload.put("propertyType", lead.getPropertyType());
        payload.put("propertyCondition", lead.getPropertyCondition());
        payload.put("score", lead.getScore());
        payload.put("tier", lead.getTier());
        payload.put("aiSummary", lead.getAiSummary());
        payload.put("tags", lead.getTags());
        payload.put("source", lead.getSource());
        payload.put("test", lead.getSource() == Lead.Source.TEST);
        payload.put("consent", lead.isConsent());

        if (url == null || url.isBlank()) {
            log.info("[webhook disabled] would send lead {} ({}, {})", lead.getId(), lead.getTier(), lead.getSource());
            return false;
        }
        try {
            client.post().uri(url).header("content-type", "application/json").body(payload).retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Webhook failed for lead {}: {}", lead.getId(), e.getMessage());
            return false;
        }
    }
}
