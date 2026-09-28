package com.hshc.backend.collecteddata;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectedDataService {

    private final CollectedDataRepository repository;

    public CollectedDataService(CollectedDataRepository repository) {
        this.repository = repository;
    }

    public CollectedData save(CollectedDataRequest request) {
        CollectedData data = new CollectedData();
        data.setFullName(request.fullName());
        data.setEmail(request.email());
        data.setPhone(request.phone());
        data.setMessage(request.message());
        return repository.save(data);
    }

    public List<CollectedData> listAll() {
        return repository.findAll();
    }
}
