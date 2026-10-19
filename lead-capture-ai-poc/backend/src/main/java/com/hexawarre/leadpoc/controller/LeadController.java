package com.hexawarre.leadpoc.controller;

import com.hexawarre.leadpoc.dto.LeadRequest;
import com.hexawarre.leadpoc.model.Lead;
import com.hexawarre.leadpoc.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @PostMapping("/leads")
    @ResponseStatus(HttpStatus.CREATED)
    public Lead submit(@Valid @RequestBody LeadRequest request,
                       @RequestParam(defaultValue = "WEB") String source) {
        Lead.Source s = "MOBILE".equalsIgnoreCase(source) ? Lead.Source.MOBILE : Lead.Source.WEB;
        return service.create(request, s, true);
    }

    @GetMapping("/leads")
    public List<Lead> list() {
        return service.all();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }
}
