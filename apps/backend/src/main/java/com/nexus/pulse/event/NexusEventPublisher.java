package com.nexus.pulse.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NexusEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NexusEventPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public NexusEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(NexusDomainEvent event) {
        log.info("Publishing domain event: type={}, eventId={}, userId={}", 
                event.getEventType(), event.getEventId(), event.getUserId());
        applicationEventPublisher.publishEvent(event);
    }
}
