package com.hexawarre.leadpoc.ai;

import com.hexawarre.leadpoc.model.Lead;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RuleBasedQualifier implements LeadQualifier {

    @Override
    public Qualification qualify(Lead lead) {
        int score = 30;
        List<String> tags = new ArrayList<>();

        if (lead.getEmail() != null && !lead.getEmail().isBlank()) {
            score += 10;
            tags.add("has-email");
        }
        if (lead.getZip() != null && lead.getZip().matches("\\d{5,6}")) {
            score += 10;
        }
        if (lead.getPropertyAddress() != null && lead.getPropertyAddress().trim().length() >= 12) {
            score += 15;
            tags.add("full-address");
        }
        String condition = lead.getPropertyCondition() == null ? "" : lead.getPropertyCondition().toUpperCase();
        switch (condition) {
            case "NEEDS_REPAIR" -> { score += 25; tags.add("motivated"); }
            case "FAIR" -> score += 10;
            case "EXCELLENT" -> score += 5;
            default -> { }
        }
        String type = lead.getPropertyType() == null ? "" : lead.getPropertyType().toUpperCase();
        if (type.equals("SINGLE_FAMILY") || type.equals("MULTI_FAMILY")) {
            score += 10;
            tags.add(type.toLowerCase().replace('_', '-'));
        }
        score = Math.min(score, 100);

        Lead.Tier tier = Qualification.tierFor(score);
        String summary = "%s lead (%d/100): %s, %s property%s."
                .formatted(tier, score,
                        lead.getPropertyType() == null ? "unspecified" : lead.getPropertyType().toLowerCase().replace('_', ' '),
                        condition.isBlank() ? "condition unknown" : condition.toLowerCase().replace('_', ' '),
                        lead.getEmail() == null || lead.getEmail().isBlank() ? ", phone only" : ", email + phone");
        return new Qualification(score, tier, summary.replace(" ,", ","), String.join(",", tags), "rule");
    }
}
