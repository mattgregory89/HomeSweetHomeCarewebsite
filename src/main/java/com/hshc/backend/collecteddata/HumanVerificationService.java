package com.hshc.backend.collecteddata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class HumanVerificationService {

    private static final long CHALLENGE_TTL_SECONDS = 300;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String signingSecret;

    public HumanVerificationService(@Value("${app.human-verification.secret:change-me-human-verification-secret}") String signingSecret) {
        this.signingSecret = signingSecret;
    }

    public HumanVerificationChallengeResponse createChallenge() {
        int left = secureRandom.nextInt(8) + 1;
        int right = secureRandom.nextInt(8) + 1;
        int answer = left + right;
        long expiresAt = Instant.now().plusSeconds(CHALLENGE_TTL_SECONDS).getEpochSecond();
        String nonce = Integer.toHexString(secureRandom.nextInt(Integer.MAX_VALUE));
        String payload = answer + ":" + expiresAt + ":" + nonce;
        String signature = sign(payload);
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
        return new HumanVerificationChallengeResponse(token, "What is " + left + " + " + right + "?");
    }

    public boolean isValid(String token, String providedAnswer) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(providedAnswer)) {
            return false;
        }

        String decodedToken;
        try {
            decodedToken = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        String[] tokenParts = decodedToken.split(":");
        if (tokenParts.length != 4) {
            return false;
        }

        String expectedAnswer = tokenParts[0];
        String expiresAtRaw = tokenParts[1];
        String nonce = tokenParts[2];
        String providedSignature = tokenParts[3];
        String payload = expectedAnswer + ":" + expiresAtRaw + ":" + nonce;

        if (!constantTimeEquals(sign(payload), providedSignature)) {
            return false;
        }

        long expiresAt;
        try {
            expiresAt = Long.parseLong(expiresAtRaw);
        } catch (NumberFormatException ex) {
            return false;
        }

        if (Instant.now().getEpochSecond() > expiresAt) {
            return false;
        }

        return expectedAnswer.equals(providedAnswer.trim());
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign human verification payload", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
