package com.hexawarre.leadpoc.service;

import com.hexawarre.leadpoc.dto.LeadRequest;
import com.hexawarre.leadpoc.model.Lead;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * QA test runner for YOUR OWN pipeline (form -> AI qualification -> webhook -> CRM).
 *
 * It only generates clearly fake records (Test User N, 555-01xx phone numbers, @example.com emails),
 * flags them as source=TEST, and sends them straight to the service layer of this app.
 * Speed and volume are adjustable and it can be stopped at any time. It never uses real people's data.
 */
@Service
public class TestRunService {

    public static final int MAX_COUNT = 100;
    public static final int MIN_DELAY_MS = 200;

    private static final String[] ZIPS = {"90001", "60601", "10001", "75201", "33101"};
    private static final String[] TYPES = {"SINGLE_FAMILY", "MULTI_FAMILY", "CONDO", "LAND"};
    private static final String[] CONDITIONS = {"EXCELLENT", "FAIR", "NEEDS_REPAIR"};

    private final LeadService leads;

    private volatile boolean running = false;
    private volatile boolean stopRequested = false;
    private volatile int total = 0;
    private volatile int sent = 0;
    private volatile int failed = 0;
    private volatile Instant startedAt;
    private final List<Map<String, Object>> log = new ArrayList<>();

    public TestRunService(LeadService leads) {
        this.leads = leads;
    }

    public synchronized Map<String, Object> start(int count, int minDelayMs, int maxDelayMs, boolean sendWebhook) {
        if (running) {
            throw new IllegalStateException("A test run is already running. Stop it first.");
        }
        int n = Math.max(1, Math.min(count, MAX_COUNT));
        int min = Math.max(MIN_DELAY_MS, minDelayMs);
        int max = Math.max(min, maxDelayMs);

        running = true;
        stopRequested = false;
        total = n;
        sent = 0;
        failed = 0;
        startedAt = Instant.now();
        synchronized (log) { log.clear(); }

        Thread t = new Thread(() -> run(n, min, max, sendWebhook), "qa-test-runner");
        t.setDaemon(true);
        t.start();
        return status();
    }

    public void stop() {
        stopRequested = true;
    }

    private void run(int n, int min, int max, boolean sendWebhook) {
        try {
            for (int i = 1; i <= n && !stopRequested; i++) {
                long t0 = System.currentTimeMillis();
                try {
                    Lead lead = leads.create(fakeRequest(i), Lead.Source.TEST, sendWebhook);
                    sent++;
                    addLog(i, lead.getId(), lead.getName(), lead.getZip(), lead.getStatus().name(),
                            lead.getTier().name(), System.currentTimeMillis() - t0, lead.isWebhookSent(), null);
                } catch (Exception e) {
                    failed++;
                    addLog(i, null, "Test User " + i, null, "ERROR", null, System.currentTimeMillis() - t0, false, e.getMessage());
                }
                if (i < n && !stopRequested) {
                    sleep(ThreadLocalRandom.current().nextLong(min, (long) max + 1));
                }
            }
        } finally {
            running = false;
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static LeadRequest fakeRequest(int i) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        LeadRequest req = new LeadRequest();
        req.setName("Test User " + i);
        req.setPhone("555-01" + String.format("%02d", i % 100));
        req.setEmail("test+" + i + "@example.com");
        req.setZip(ZIPS[r.nextInt(ZIPS.length)]);
        req.setPropertyAddress((100 + i) + " Test Street (fake)");
        req.setPropertyType(TYPES[r.nextInt(TYPES.length)]);
        req.setPropertyCondition(CONDITIONS[r.nextInt(CONDITIONS.length)]);
        req.setConsent(true);
        return req;
    }

    private void addLog(int n, Long leadId, String name, String zip, String status, String tier,
                        long ms, boolean webhookSent, String error) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("n", n);
        e.put("time", Instant.now().toString());
        e.put("leadId", leadId);
        e.put("name", name);
        e.put("zip", zip);
        e.put("status", status);
        e.put("tier", tier);
        e.put("ms", ms);
        e.put("webhookSent", webhookSent);
        e.put("error", error);
        synchronized (log) {
            log.add(e);
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("running", running);
        m.put("total", total);
        m.put("sent", sent);
        m.put("failed", failed);
        m.put("startedAt", startedAt);
        synchronized (log) {
            m.put("log", new ArrayList<>(log));
        }
        return m;
    }
}
