package com.hshc.backend.collecteddata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/recaptcha")
public class RecaptchaController {
    private final RecaptchaService service;
    public RecaptchaController(RecaptchaService service) { this.service = service; }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> config() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("siteKey", service.getSiteKey()));
    }
}
