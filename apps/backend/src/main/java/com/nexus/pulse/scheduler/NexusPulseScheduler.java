package com.nexus.pulse.scheduler;

import com.nexus.pulse.service.MonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "nexus.pulse.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class NexusPulseScheduler {

    private static final Logger log = LoggerFactory.getLogger(NexusPulseScheduler.class);
    private final MonitoringService monitoringService;

    public NexusPulseScheduler(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Scheduled(cron = "${nexus.pulse.scheduler.cron:0 0 * * * *}")
    public void runScheduledMonitoring() {
        log.info("Executing scheduled NEXUS Pulse monitoring...");
        try {
            monitoringService.runFullMonitoringCycle();
        } catch (Exception e) {
            log.error("Error during scheduled NEXUS Pulse monitoring: {}", e.getMessage(), e);
        }
    }
}
