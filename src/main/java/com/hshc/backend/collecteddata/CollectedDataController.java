package com.hshc.backend.collecteddata;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/collected-data")
public class CollectedDataController {

    private final CollectedDataService service;

    public CollectedDataController(CollectedDataService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectedData create(@Valid @RequestBody CollectedDataRequest request) {
        return service.save(request);
    }

    @GetMapping
    public List<CollectedData> list() {
        return service.listAll();
    }
}
