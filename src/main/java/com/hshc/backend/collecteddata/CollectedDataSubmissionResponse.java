package com.hshc.backend.collecteddata;

import java.time.LocalDateTime;

public record CollectedDataSubmissionResponse(
        Long id,
        LocalDateTime submittedAt,
        boolean confirmationEmailSent,
        String message
) {
}
