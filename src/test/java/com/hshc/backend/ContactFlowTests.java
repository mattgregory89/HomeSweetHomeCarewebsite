package com.hshc.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hshc.backend.collecteddata.CollectedDataRepository;
import com.hshc.backend.collecteddata.AdminCollectedDataController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class ContactFlowTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CollectedDataRepository repository;

    @Test
    void landingAndSupportingPagesArePackaged() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
            .andExpect(content().string(containsString("like <em>home.</em>")))
            .andExpect(content().string(containsString("humanVerificationAnswer")))
            .andExpect(content().string(containsString("assistant-input")));
        for (String path : new String[]{"/styles.css", "/script.js", "/assets/home-sweet-home-logo.png",
                "/services.html", "/scheduling.html", "/faq.html", "/admin.html"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void validInquiryIsSavedEvenWhenSmtpIsUnavailable() throws Exception {
        JsonNode challenge = json.readTree(mvc.perform(get("/api/human-verification/challenge"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String[] numbers = challenge.get("prompt").asText().replaceAll("[^0-9]+", " ").trim().split(" +");
        String answer = String.valueOf(Integer.parseInt(numbers[0]) + Integer.parseInt(numbers[1]));
        long before = repository.count();
        mvc.perform(post("/api/collected-data").contentType("application/json")
            .content(json.writeValueAsString(Map.of("fullName", "Test Visitor", "email", "visitor@example.com",
                "phone", "555-0100", "message", "Please discuss companionship.",
                "humanVerificationToken", challenge.get("token").asText(), "humanVerificationAnswer", answer))))
            .andExpect(status().isCreated()).andExpect(jsonPath("confirmationEmailSent").value(false))
            .andExpect(jsonPath("id").isNumber());
        assertEquals(before + 1, repository.count());
        mvc.perform(get("/api/admin/collected-data")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/collected-data").header("X-Admin-Key", "test-admin-key"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("visitor@example.com"));
        mvc.perform(get("/api/collected-data")).andExpect(status().is4xxClientError());
    }

    @Test
    void badVerificationAndMalformedRequestsDoNotSave() throws Exception {
        long before = repository.count();
        mvc.perform(post("/api/collected-data").contentType("application/json")
            .content(json.writeValueAsString(Map.of("fullName", "Test", "email", "test@example.com", "message", "Hello",
                "humanVerificationToken", "invalid", "humanVerificationAnswer", "1"))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("message").exists());
        mvc.perform(post("/api/collected-data").contentType("application/json").content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("message").exists());
        assertEquals(before, repository.count());
    }

    @Test
    void unsetAdminKeyDisablesAccess() {
        var controller = new AdminCollectedDataController(null, "");
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.list("change-me"));
    }
}
