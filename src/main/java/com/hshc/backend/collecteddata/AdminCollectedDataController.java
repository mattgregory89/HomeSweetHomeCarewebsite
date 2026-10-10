package com.hshc.backend.collecteddata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/collected-data")
public class AdminCollectedDataController {

    private final CollectedDataService service;
    private final String adminApiKey;

    public AdminCollectedDataController(CollectedDataService service,
                                        @Value("${app.admin.api-key:}") String adminApiKey) {
        this.service = service;
        this.adminApiKey = adminApiKey;
    }

    @GetMapping
    public List<CollectedData> list(@RequestHeader(name = "X-Admin-Key", required = false) String providedApiKey) {
        if (!StringUtils.hasText(adminApiKey) || !StringUtils.hasText(providedApiKey) || !providedApiKey.equals(adminApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin key");
        }
        return service.listAll();
    }
}
