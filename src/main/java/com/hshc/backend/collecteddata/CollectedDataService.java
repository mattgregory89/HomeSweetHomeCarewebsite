package com.hshc.backend.collecteddata;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.http.HttpStatus;

@Service
public class CollectedDataService {

    private final CollectedDataRepository repository;
    private final ConfirmationEmailService confirmationEmailService;
    private final HumanVerificationService humanVerificationService;
    private final EmailValidationService emailValidationService;

    public CollectedDataService(CollectedDataRepository repository,
                                ConfirmationEmailService confirmationEmailService,
                                HumanVerificationService humanVerificationService,
                                EmailValidationService emailValidationService) {
        this.repository = repository;
        this.confirmationEmailService = confirmationEmailService;
        this.humanVerificationService = humanVerificationService;
        this.emailValidationService = emailValidationService;
    }

    public CollectedDataSubmissionResult save(CollectedDataRequest request) {
        if (!humanVerificationService.isValid(request.humanVerificationToken(), request.humanVerificationAnswer())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Human verification failed. Please try again.");
        }

        String normalizedEmail = request.email().trim();
        if (!emailValidationService.isValid(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid email address.");
        }

        CollectedData data = new CollectedData();
        data.setFullName(request.fullName());
        data.setEmail(normalizedEmail);
        data.setPhone(request.phone());
        data.setMessage(request.message());
        CollectedData saved = repository.save(data);

        boolean emailSent = confirmationEmailService.sendConfirmation(saved);
        return new CollectedDataSubmissionResult(saved, emailSent);
    }

    public List<CollectedData> listAll() {
        return repository.findAllByOrderBySubmittedAtDesc();
    }
}
