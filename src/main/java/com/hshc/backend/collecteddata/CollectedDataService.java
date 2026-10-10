package com.hshc.backend.collecteddata;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectedDataService {

    private final CollectedDataRepository repository;
    private final ConfirmationEmailService confirmationEmailService;

    public CollectedDataService(CollectedDataRepository repository, ConfirmationEmailService confirmationEmailService) {
        this.repository = repository;
        this.confirmationEmailService = confirmationEmailService;
    }

    public CollectedDataSubmissionResult save(CollectedDataRequest request) {
        CollectedData data = new CollectedData();
        data.setFullName(request.fullName());
        data.setEmail(request.email());
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
