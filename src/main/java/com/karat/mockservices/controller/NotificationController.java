package com.karat.mockservices.controller;

import com.karat.mockservices.model.notification.*;
import com.karat.mockservices.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * POST /api/notifications/send
     *
     * Request: { "userId": "USR-001", "orderId": "ORD-001", "channel": "EMAIL" }
     *
     * Always returns HTTP 200 with QUEUED 😈
     * Does NOT guarantee delivery!
     *
     * Channels: EMAIL, SMS, PUSH
     * - channel = "EMAIL"  → QUEUED (normal)
     * - channel = "SMS"    → QUEUED but takes 2 seconds
     * - channel = "PUSH"   → randomly QUEUED or DROPPED 😈
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@RequestBody NotificationRequest request) throws InterruptedException {
        return ResponseEntity.ok(notificationService.send(request));
    }

    /**
     * GET /api/notifications/status/{notificationId}
     *
     * - NOTIF-DELIVERED  → DELIVERED
     * - NOTIF-FAILED     → FAILED (email bounced)
     * - anything         → DELIVERED after 2 polls
     */
    @GetMapping("/status/{notificationId}")
    public ResponseEntity<NotificationStatusResponse> getStatus(@PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.getStatus(notificationId));
    }
}
