package com.hshc.backend.collecteddata;

public record HumanVerificationChallengeResponse(
        String token,
        String prompt
) {
}
