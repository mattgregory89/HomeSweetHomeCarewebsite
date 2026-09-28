package com.hshc.backend.collecteddata;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectedDataRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 50) String phone,
        @NotBlank @Size(max = 2000) String message
) {
}
