package com.nexus.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        // Limit to 3 requests per 60 seconds for test
        filter = new RateLimitingFilter(true, 3, 60, objectMapper);
    }

    @Test
    @DisplayName("Allows requests within limit for auth endpoints")
    void testAllowsRequestsWithinLimit() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Blocks requests exceeding limit for auth endpoints with 429")
    void testBlocksRequestsExceedingLimit() throws Exception {
        String clientIp = "192.168.1.101";

        for (int i = 0; i < 3; i++) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, chain);
            assertEquals(200, res.getStatus());
        }

        // 4th request should be blocked
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        blockedReq.setRemoteAddr(clientIp);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilterInternal(blockedReq, blockedRes, chain);

        assertEquals(429, blockedRes.getStatus());
        assertEquals("60", blockedRes.getHeader("Retry-After"));
    }

    @Test
    @DisplayName("Non-auth endpoints are unaffected by auth rate limiting")
    void testNonAuthEndpointsBypassed() throws Exception {
        String clientIp = "192.168.1.102";

        for (int i = 0; i < 10; i++) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/opportunities");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, chain);
            assertEquals(200, res.getStatus());
        }
    }
}
