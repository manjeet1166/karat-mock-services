package com.karat.mockservices.service;

import com.karat.mockservices.model.notification.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NotificationService {

    private final Random random = new Random();
    private final Map<String, AtomicInteger> statusPollCounter = new ConcurrentHashMap<>();

    public NotificationResponse send(NotificationRequest request) throws InterruptedException {

        String channel = request.getChannel() == null ? "EMAIL" : request.getChannel().toUpperCase();

        // SMS is slow
        if ("SMS".equals(channel)) {
            Thread.sleep(2000);
        }

        // PUSH randomly drops 😈
        if ("PUSH".equals(channel) && random.nextBoolean()) {
            return NotificationResponse.builder()
                    .notificationId("NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("DROPPED")
                    .channel(channel)
                    .queuedAt(Instant.now().toString())
                    .warning("Push notification was silently dropped. Device token may be stale.")
                    .build();
        }

        // Always returns 200 QUEUED — does NOT mean delivered!
        return NotificationResponse.builder()
                .notificationId("NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("QUEUED")
                .channel(channel)
                .queuedAt(Instant.now().toString())
                .warning("Delivery not guaranteed. Use /status/{notificationId} to track.")
                .build();
    }

    public NotificationStatusResponse getStatus(String notificationId) {

        if ("NOTIF-DELIVERED".equals(notificationId)) {
            return NotificationStatusResponse.builder()
                    .notificationId(notificationId)
                    .status("DELIVERED")
                    .deliveredAt(Instant.now().toString())
                    .build();
        }

        if ("NOTIF-FAILED".equals(notificationId)) {
            return NotificationStatusResponse.builder()
                    .notificationId(notificationId)
                    .status("FAILED")
                    .failureReason("EMAIL_BOUNCED")
                    .build();
        }

        // Resolves to DELIVERED after 2 polls
        int count = statusPollCounter
                .computeIfAbsent(notificationId, k -> new AtomicInteger(0))
                .incrementAndGet();

        if (count >= 2) {
            return NotificationStatusResponse.builder()
                    .notificationId(notificationId)
                    .status("DELIVERED")
                    .deliveredAt(Instant.now().toString())
                    .build();
        }

        return NotificationStatusResponse.builder()
                .notificationId(notificationId)
                .status("PENDING")
                .build();
    }
}
