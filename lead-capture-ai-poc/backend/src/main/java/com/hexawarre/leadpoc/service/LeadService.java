package com.hexawarre.leadpoc.service;

import com.hexawarre.leadpoc.ai.LeadQualifier;
import com.hexawarre.leadpoc.ai.Qualification;
import com.hexawarre.leadpoc.dto.LeadRequest;
import com.hexawarre.leadpoc.model.Lead;
import com.hexawarre.leadpoc.repo.LeadRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeadService {

    public static final String CONSENT_TEXT =
            "I agree to be contacted by phone, SMS or email about my request. Message/data rates may apply.";

    private final LeadRepository repo;
    private final LeadQualifier qualifier;
    private final WebhookService webhook;

    public LeadService(LeadRepository repo, LeadQualifier qualifier, WebhookService webhook) {
        this.repo = repo;
        this.qualifier = qualifier;
        this.webhook = webhook;
    }

    public Lead create(LeadRequest req, Lead.Source source, boolean forwardToWebhook) {
        Lead lead = new Lead();
        lead.setName(req.getName().trim());
        lead.setPhone(req.getPhone().trim());
        lead.setEmail(req.getEmail() == null || req.getEmail().isBlank() ? null : req.getEmail().trim());
        lead.setZip(req.getZip().trim());
        lead.setPropertyAddress(req.getPropertyAddress().trim());
        lead.setPropertyType(req.getPropertyType());
        lead.setPropertyCondition(req.getPropertyCondition());
        lead.setConsent(req.isConsent());
        lead.setConsentText(source == Lead.Source.TEST
                ? "Automated QA test record (fake data, no real person)" : CONSENT_TEXT);
        lead.setSource(source);

        if (source != Lead.Source.TEST && repo.existsByPhoneAndSource(lead.getPhone(), source)) {
            lead.setStatus(Lead.Status.DUPLICATE);
            lead.setTier(Lead.Tier.COLD);
            lead.setAiSummary("Duplicate: this phone number was already submitted.");
            lead.setAiProvider("rule");
            return repo.save(lead);
        }

        Qualification q = qualifier.qualify(lead);
        lead.setScore(q.score());
        lead.setTier(q.tier());
        lead.setAiSummary(q.summary());
        lead.setTags(q.tags());
        lead.setAiProvider(q.provider());
        lead.setStatus(q.tier() == Lead.Tier.COLD ? Lead.Status.NEW : Lead.Status.QUALIFIED);
        lead = repo.save(lead);

        if (forwardToWebhook) {
            lead.setWebhookSent(webhook.send(lead));
            lead = repo.save(lead);
        }
        return lead;
    }

    public List<Lead> all() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", repo.count());
        m.put("hot", repo.countByTier(Lead.Tier.HOT));
        m.put("warm", repo.countByTier(Lead.Tier.WARM));
        m.put("cold", repo.countByTier(Lead.Tier.COLD));
        m.put("duplicates", repo.countByStatus(Lead.Status.DUPLICATE));
        m.put("web", repo.countBySource(Lead.Source.WEB));
        m.put("mobile", repo.countBySource(Lead.Source.MOBILE));
        m.put("test", repo.countBySource(Lead.Source.TEST));
        return m;
    }

    public void deleteTestLeads() {
        repo.deleteAll(repo.findAll().stream().filter(l -> l.getSource() == Lead.Source.TEST).toList());
    }
}
