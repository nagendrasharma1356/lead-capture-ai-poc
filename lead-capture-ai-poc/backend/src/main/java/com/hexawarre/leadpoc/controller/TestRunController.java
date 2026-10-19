package com.hexawarre.leadpoc.controller;

import com.hexawarre.leadpoc.service.LeadService;
import com.hexawarre.leadpoc.service.TestRunService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test-runs")
public class TestRunController {

    public record StartRequest(Integer count, Integer minDelayMs, Integer maxDelayMs, Boolean sendWebhook) { }

    private final TestRunService runner;
    private final LeadService leads;

    public TestRunController(TestRunService runner, LeadService leads) {
        this.runner = runner;
        this.leads = leads;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody(required = false) StartRequest req) {
        int count = req == null || req.count() == null ? 10 : req.count();
        int min = req == null || req.minDelayMs() == null ? 1000 : req.minDelayMs();
        int max = req == null || req.maxDelayMs() == null ? 3000 : req.maxDelayMs();
        boolean webhook = req != null && Boolean.TRUE.equals(req.sendWebhook());
        return runner.start(count, min, max, webhook);
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        runner.stop();
        return runner.status();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return runner.status();
    }

    @DeleteMapping("/data")
    public Map<String, String> clear() {
        leads.deleteTestLeads();
        return Map.of("message", "Test leads deleted");
    }
}
