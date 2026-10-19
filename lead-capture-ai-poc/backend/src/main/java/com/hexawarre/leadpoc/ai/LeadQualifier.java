package com.hexawarre.leadpoc.ai;

import com.hexawarre.leadpoc.model.Lead;

public interface LeadQualifier {
    Qualification qualify(Lead lead);
}
