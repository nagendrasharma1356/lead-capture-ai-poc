package com.hexawarre.leadpoc.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "leads")
public class Lead {

    public enum Source { WEB, MOBILE, TEST }
    public enum Tier { HOT, WARM, COLD }
    public enum Status { NEW, QUALIFIED, DUPLICATE, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String zip;
    private String propertyAddress;
    private String propertyType;
    private String propertyCondition;

    /** The person ticked the consent box on the form. Required for every real lead. */
    private boolean consent;
    private String consentText;

    @Enumerated(EnumType.STRING)
    private Source source;

    private int score;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 1000)
    private String aiSummary;
    private String aiProvider;
    private String tags;

    private boolean webhookSent;
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public String getPropertyCondition() { return propertyCondition; }
    public void setPropertyCondition(String propertyCondition) { this.propertyCondition = propertyCondition; }
    public boolean isConsent() { return consent; }
    public void setConsent(boolean consent) { this.consent = consent; }
    public String getConsentText() { return consentText; }
    public void setConsentText(String consentText) { this.consentText = consentText; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public Tier getTier() { return tier; }
    public void setTier(Tier tier) { this.tier = tier; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public boolean isWebhookSent() { return webhookSent; }
    public void setWebhookSent(boolean webhookSent) { this.webhookSent = webhookSent; }
    public Instant getCreatedAt() { return createdAt; }
}
