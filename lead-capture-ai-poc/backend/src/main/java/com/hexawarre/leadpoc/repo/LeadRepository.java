package com.hexawarre.leadpoc.repo;

import com.hexawarre.leadpoc.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findAllByOrderByCreatedAtDesc();
    boolean existsByPhoneAndSourceNot(String phone, Lead.Source source);
    boolean existsByPhoneAndSource(String phone, Lead.Source source);
    long countByStatus(Lead.Status status);
    long countByTier(Lead.Tier tier);
    long countBySource(Lead.Source source);
}
