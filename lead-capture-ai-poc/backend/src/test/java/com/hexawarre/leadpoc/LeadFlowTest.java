package com.hexawarre.leadpoc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
@AutoConfigureMockMvc
class LeadFlowTest {

    @Autowired
    MockMvc mvc;

    private static String body(boolean consent, String phone) {
        return """
                {"name":"Asha Verma","phone":"%s","email":"asha@example.com","zip":"60601",
                 "propertyAddress":"221 Lake Street, Chicago","propertyType":"SINGLE_FAMILY",
                 "propertyCondition":"NEEDS_REPAIR","consent":%s}
                """.formatted(phone, consent);
    }

    @Test
    void rejectsSubmissionWithoutConsent() throws Exception {
        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(body(false, "312-555-0111")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.consent").exists());
    }

    @Test
    void qualifiesLeadAndFlagsDuplicate() throws Exception {
        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(body(true, "312-555-0122")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tier").value("HOT"))
                .andExpect(jsonPath("$.status").value("QUALIFIED"));

        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON).content(body(true, "312-555-0122")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DUPLICATE"));
    }

    @Test
    void testRunnerStartsAndReportsStatus() throws Exception {
        mvc.perform(post("/api/test-runs/start").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":2,\"minDelayMs\":200,\"maxDelayMs\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
        mvc.perform(get("/api/test-runs/status")).andExpect(status().isOk());
        mvc.perform(post("/api/test-runs/stop")).andExpect(status().isOk());
    }
}
