package com.hshc.backend.collecteddata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/human-verification")
public class HumanVerificationController {

    private final HumanVerificationService humanVerificationService;

    public HumanVerificationController(HumanVerificationService humanVerificationService) {
        this.humanVerificationService = humanVerificationService;
    }

    @GetMapping("/challenge")
    public HumanVerificationChallengeResponse getChallenge() {
        return humanVerificationService.createChallenge();
    }
}
