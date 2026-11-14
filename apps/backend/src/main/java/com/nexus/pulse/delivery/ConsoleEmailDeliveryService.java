package com.nexus.pulse.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConsoleEmailDeliveryService implements EmailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailDeliveryService.class);

    @Override
    public boolean sendNotificationEmail(UUID userId, String recipientEmail, String subject, String content) {
        log.info("[MOCK EMAIL DELIVERY] To: {} (User: {}), Subject: '{}', Body: '{}'", 
                recipientEmail, userId, subject, content);
        return true;
    }
}
