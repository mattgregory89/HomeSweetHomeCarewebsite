package com.hshc.backend.collecteddata;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collected-data")
public class CollectedDataController {

    private final CollectedDataService service;

    public CollectedDataController(CollectedDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectedDataSubmissionResponse create(@Valid @RequestBody CollectedDataRequest request) {
        CollectedDataSubmissionResult result = service.save(request);
        return new CollectedDataSubmissionResponse(
                result.data().getId(),
                result.data().getSubmittedAt(),
                result.confirmationEmailSent(),
                result.confirmationEmailSent()
                        ? "Thanks for contacting us. A confirmation email has been sent."
                        : "Thanks for contacting us. We received your request but could not send a confirmation email right now."
        );
    }
}
