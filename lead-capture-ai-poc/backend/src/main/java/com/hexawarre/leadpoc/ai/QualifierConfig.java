package com.hexawarre.leadpoc.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class QualifierConfig {

    @Bean
    @Primary
    public LeadQualifier activeQualifier(@Value("${app.ai.provider}") String provider,
                                         RuleBasedQualifier rule,
                                         ClaudeQualifier claude) {
        return "claude".equalsIgnoreCase(provider) ? claude : rule;
    }
}
