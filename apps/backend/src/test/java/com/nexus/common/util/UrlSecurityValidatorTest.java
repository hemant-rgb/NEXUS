package com.nexus.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlSecurityValidatorTest {

    @Test
    @DisplayName("Validates clean public HTTP and HTTPS URLs")
    void testValidPublicUrls() {
        assertDoesNotThrow(() -> UrlSecurityValidator.validateExternalUrl("https://www.google.com", false));
        assertDoesNotThrow(() -> UrlSecurityValidator.validateExternalUrl("https://api.github.com/repos", false));
    }

    @Test
    @DisplayName("Rejects malicious non-HTTP protocols")
    void testRejectsNonHttpProtocols() {
        assertThrows(IllegalArgumentException.class, () -> 
                UrlSecurityValidator.validateExternalUrl("file:///etc/passwd", false));
        assertThrows(IllegalArgumentException.class, () -> 
                UrlSecurityValidator.validateExternalUrl("ftp://ftp.example.com", false));
        assertThrows(IllegalArgumentException.class, () -> 
                UrlSecurityValidator.validateExternalUrl("gopher://localhost:70", false));
    }

    @Test
    @DisplayName("Rejects loopback and private IP addresses when private IPs are forbidden")
    void testRejectsLoopbackAndPrivateIps() {
        assertThrows(SecurityException.class, () -> 
                UrlSecurityValidator.validateExternalUrl("http://127.0.0.1:8080/admin", false));
        assertThrows(SecurityException.class, () -> 
                UrlSecurityValidator.validateExternalUrl("http://localhost:8080", false));
    }
}
