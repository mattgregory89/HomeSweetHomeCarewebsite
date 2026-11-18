package com.hshc.backend.collecteddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecaptchaService {
    private final RestTemplate client;
    private final String siteKey;
    private final String secretKey;

    public RecaptchaService(RestTemplate recaptchaRestTemplate,
                            @Value("${app.recaptcha.site-key:}") String siteKey,
                            @Value("${app.recaptcha.secret-key:}") String secretKey) {
        this.client = recaptchaRestTemplate;
        this.siteKey = siteKey;
        this.secretKey = secretKey;
    }

    public String getSiteKey() {
        requireConfiguration();
        return siteKey;
    }

    private void requireConfiguration() {
        if (!StringUtils.hasText(siteKey) || !StringUtils.hasText(secretKey)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Contact verification is not configured yet. Please try again later.");
        }
    }

    public boolean isValid(String token) {
        requireConfiguration();
        if (!StringUtils.hasText(token)) return false;
        var body = new LinkedMultiValueMap<String, String>();
        body.add("secret", secretKey);
        body.add("response", token);
        try {
            VerificationResponse response = client.postForObject(
                    "https://www.google.com/recaptcha/api/siteverify", body, VerificationResponse.class);
            if (response != null && response.errorCodes() != null &&
                    response.errorCodes().stream().anyMatch(code -> code.equals("invalid-input-secret") || code.equals("missing-input-secret"))) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Contact verification is not configured correctly. Please try again later.");
            }
            return response != null && response.success();
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to reach contact verification. Please try again shortly.");
        }
    }

    public record VerificationResponse(boolean success,
            @JsonProperty("error-codes") java.util.List<String> errorCodes) {}
}
