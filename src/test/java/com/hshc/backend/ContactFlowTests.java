package com.hshc.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import com.hshc.backend.collecteddata.RecaptchaService;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
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
    @Autowired RestTemplate recaptchaRestTemplate;
    MockRestServiceServer google;

    @BeforeEach
    void setupGoogle() { google = MockRestServiceServer.bindTo(recaptchaRestTemplate).build(); }
    @AfterEach
    void checkGoogle() { google.verify(); }

    void expectVerification(boolean success) {
        google.expect(requestTo("https://www.google.com/recaptcha/api/siteverify"))
            .andExpect(method(org.springframework.http.HttpMethod.POST))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().string(containsString("secret=test-secret-key")))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().string(containsString("response=")))
            .andRespond(withSuccess("{\"success\":" + success + "}", MediaType.APPLICATION_JSON));
    }

    @Test
    void landingAndSupportingPagesArePackaged() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
            .andExpect(content().string(containsString("like <em>home.</em>")))
            .andExpect(content().string(containsString("recaptcha-widget")))
            .andExpect(content().string(containsString("assistant-input")));
        for (String path : new String[]{"/styles.css", "/script.js", "/assets/home-sweet-home-logo.png",
                "/services.html", "/scheduling.html", "/faq.html", "/admin.html"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void validInquiryIsSavedEvenWhenSmtpIsUnavailable() throws Exception {
        expectVerification(true);
        long before = repository.count();
        mvc.perform(post("/api/collected-data").contentType("application/json")
            .content(json.writeValueAsString(Map.of("fullName", "Test Visitor", "email", "visitor@example.com",
                "phone", "555-0100", "message", "Please discuss companionship.",
                "recaptchaToken", "valid-google-token"))))
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
        expectVerification(false);
        long before = repository.count();
        mvc.perform(post("/api/collected-data").contentType("application/json")
            .content(json.writeValueAsString(Map.of("fullName", "Test", "email", "test@example.com", "message", "Hello",
                "recaptchaToken", "invalid"))))
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

    @Test
    void configExposesOnlySiteKey() throws Exception {
        mvc.perform(get("/api/recaptcha/config")).andExpect(status().isOk())
            .andExpect(jsonPath("siteKey").value("test-site-key"))
            .andExpect(jsonPath("secretKey").doesNotExist());
    }

    @Test
    void missingKeysPreventVerification() {
        var service = new RecaptchaService(recaptchaRestTemplate, "", "");
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> service.isValid("token"));
        assertEquals(503, error.getStatusCode().value());
    }

    @Test
    void googleFailureDoesNotSaveInquiry() throws Exception {
        google.expect(requestTo("https://www.google.com/recaptcha/api/siteverify"))
            .andRespond(withServerError());
        long before = repository.count();
        mvc.perform(post("/api/collected-data").contentType("application/json")
            .content(json.writeValueAsString(Map.of("fullName", "Test", "email", "test@example.com",
                "message", "Hello", "recaptchaToken", "token"))))
            .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("message").exists());
        assertEquals(before, repository.count());
    }
}
