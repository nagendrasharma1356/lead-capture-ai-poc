package com.hexawarre.leadpoc.ai;

import com.hexawarre.leadpoc.model.Lead;

public record Qualification(int score, Lead.Tier tier, String summary, String tags, String provider) {

    public static Lead.Tier tierFor(int score) {
        if (score >= 70) return Lead.Tier.HOT;
        if (score >= 45) return Lead.Tier.WARM;
        return Lead.Tier.COLD;
    }
}
