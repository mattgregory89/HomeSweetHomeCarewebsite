package com.hshc.backend.collecteddata;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailValidationService {

    public boolean isValid(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        String normalized = email.trim();
        if (normalized.length() > 200 || normalized.contains(" ")) {
            return false;
        }

        int atIndex = normalized.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == normalized.length() - 1) {
            return false;
        }

        String domain = normalized.substring(atIndex + 1);
        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }

        try {
            InternetAddress address = new InternetAddress(normalized);
            address.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}
