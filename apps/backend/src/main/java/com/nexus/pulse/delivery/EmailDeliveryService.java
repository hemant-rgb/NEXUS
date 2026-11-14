package com.nexus.pulse.delivery;

import java.util.UUID;

public interface EmailDeliveryService {
    boolean sendNotificationEmail(UUID userId, String recipientEmail, String subject, String content);
}
