package com.nexus.source;

import com.nexus.source.connector.http.IngestionHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestionHttpClientTest {

    private final IngestionHttpClient client = new IngestionHttpClient();

    @Test
    @DisplayName("SSRF Defense: should accept valid public HTTPS endpoints")
    void testValidPublicUrls() {
        client.validateUrlSafety("https://remotive.com/api/remote-jobs");
        client.validateUrlSafety("https://www.arbeitnow.com/api/job-board-api");
        client.validateUrlSafety("https://summerofcode.withgoogle.com");
    }

    @Test
    @DisplayName("SSRF Defense: should reject non-HTTPS schemes")
    void testRejectNonHttps() {
        assertThatThrownBy(() -> client.validateUrlSafety("http://remotive.com/api/jobs"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Strictly HTTPS permitted");

        assertThatThrownBy(() -> client.validateUrlSafety("ftp://example.com"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("SSRF Defense: should reject localhost, loopback, and private IPv4 ranges")
    void testRejectPrivateIpsAndLocalhost() {
        assertThatThrownBy(() -> client.validateUrlSafety("https://localhost/api"))
                .isInstanceOf(SecurityException.class);

        assertThatThrownBy(() -> client.validateUrlSafety("https://127.0.0.1:8080/metrics"))
                .isInstanceOf(SecurityException.class);

        assertThatThrownBy(() -> client.validateUrlSafety("https://10.0.1.5/admin"))
                .isInstanceOf(SecurityException.class);

        assertThatThrownBy(() -> client.validateUrlSafety("https://192.168.1.1/router"))
                .isInstanceOf(SecurityException.class);

        assertThatThrownBy(() -> client.validateUrlSafety("https://172.16.0.1"))
                .isInstanceOf(SecurityException.class);

        assertThatThrownBy(() -> client.validateUrlSafety("https://cluster.local"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("SSRF Defense: should reject cloud metadata IP (169.254.169.254)")
    void testRejectCloudMetadata() {
        assertThatThrownBy(() -> client.validateUrlSafety("https://169.254.169.254/latest/meta-data"))
                .isInstanceOf(SecurityException.class);
    }
}
