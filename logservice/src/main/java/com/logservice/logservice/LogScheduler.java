package com.logservice.logservice;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class LogScheduler {

    private final AtomicInteger counter = new AtomicInteger(0);

    private String currentTraceId = UUID.randomUUID().toString();
    private String currentRequestId = UUID.randomUUID().toString();
    private String currentSpanId = UUID.randomUUID().toString();

    @Scheduled(fixedRate = 10000)
    public void logEveryTenSeconds() {
        int count = counter.incrementAndGet();

        // Update IDs according to the rules
        if (count % 10 == 1) {
            currentTraceId = "trace-" + randomId(5) + "-" + count;
        }
        if (count % 5 == 1) {
            currentRequestId = "request-" + randomId(5) + "-" + count;
        }
        if (count % 2 == 1) {
            currentSpanId = "span-" + randomId(5) + "-" + count;
        }

        // Set MDC context for logging
        MDC.put("traceId", currentTraceId);
        MDC.put("requestId", currentRequestId);
        MDC.put("spanId", currentSpanId);

        // Log info or error
        if (count % 5 == 0) {
            log.error("Error occurred on log number {}", count);
            throw new RuntimeException("Error occurred on log number " + count);
        } else {
            log.info("Running scheduled task #{}", count);
        }

        MDC.clear();
    }

    private static String randomId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
