package com.karat.mockservices.service;

import com.karat.mockservices.model.payment.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PaymentService {

    // Idempotency store: key → response
    private final Map<String, PaymentResponse> idempotencyCache = new ConcurrentHashMap<>();

    // Poll counter for PENDING resolution
    private final Map<String, AtomicInteger> pollCounter = new ConcurrentHashMap<>();

    public ResponseEntity<PaymentResponse> charge(PaymentRequest request) throws InterruptedException {

        // --- Idempotency check ---
        // Same idempotencyKey = return same response (no double charge)
        if (request.getIdempotencyKey() != null
                && idempotencyCache.containsKey(request.getIdempotencyKey())) {
            return ResponseEntity.ok(idempotencyCache.get(request.getIdempotencyKey()));
        }

        double amount = request.getAmount();

        // HTTP 503 — service down
        if (amount == 503) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(PaymentResponse.builder()
                            .status("ERROR")
                            .message("Payment service temporarily unavailable")
                            .build());
        }

        // Timeout simulation — 5 second sleep
        if (amount > 500) {
            Thread.sleep(5000);
            // After sleep, still return timeout-like ambiguous response
            PaymentResponse response = PaymentResponse.builder()
                    .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("PENDING")
                    .amount(amount)
                    .message("Request timed out — payment status unknown. Use /status endpoint to verify.")
                    .build();
            cacheIfIdempotent(request.getIdempotencyKey(), response);
            return ResponseEntity.ok(response);
        }

        // Insufficient funds
        if (amount == 999) {
            PaymentResponse response = PaymentResponse.builder()
                    .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("FAILED")
                    .amount(amount)
                    .failureReason("INSUFFICIENT_FUNDS")
                    .build();
            cacheIfIdempotent(request.getIdempotencyKey(), response);
            return ResponseEntity.ok(response);
        }

        // PENDING — ambiguous state 😈
        if (amount >= 100 && amount <= 500) {
            PaymentResponse response = PaymentResponse.builder()
                    .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .status("PENDING")
                    .amount(amount)
                    .processedAt(null) // null = not settled
                    .message("Payment is being processed. Poll /status/{transactionId} for updates.")
                    .build();
            cacheIfIdempotent(request.getIdempotencyKey(), response);
            return ResponseEntity.ok(response);
        }

        // SUCCESS — amount < 100
        PaymentResponse response = PaymentResponse.builder()
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("SUCCESS")
                .amount(amount)
                .processedAt(Instant.now().toString())
                .build();
        cacheIfIdempotent(request.getIdempotencyKey(), response);
        return ResponseEntity.ok(response);
    }

    public PaymentStatusResponse getStatus(String transactionId) {
        // Special known IDs
        if ("TXN-SETTLED".equals(transactionId)) {
            return PaymentStatusResponse.builder()
                    .transactionId(transactionId)
                    .status("SUCCESS")
                    .settledAt(Instant.now().toString())
                    .build();
        }

        if ("TXN-FAILED".equals(transactionId)) {
            return PaymentStatusResponse.builder()
                    .transactionId(transactionId)
                    .status("FAILED")
                    .failureReason("CARD_DECLINED")
                    .build();
        }

        if ("TXN-PENDING".equals(transactionId)) {
            return PaymentStatusResponse.builder()
                    .transactionId(transactionId)
                    .status("PENDING")
                    .build();
        }

        // For anything else: resolves to SUCCESS after 3 polls
        int count = pollCounter
                .computeIfAbsent(transactionId, k -> new AtomicInteger(0))
                .incrementAndGet();

        if (count >= 3) {
            return PaymentStatusResponse.builder()
                    .transactionId(transactionId)
                    .status("SUCCESS")
                    .settledAt(Instant.now().toString())
                    .build();
        }

        return PaymentStatusResponse.builder()
                .transactionId(transactionId)
                .status("PENDING")
                .build();
    }

    public RefundResponse refund(RefundRequest request) {
        return RefundResponse.builder()
                .refundId("RFD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .status("REFUNDED")
                .processedAt(Instant.now().toString())
                .build();
    }

    private void cacheIfIdempotent(String key, PaymentResponse response) {
        if (key != null && !key.isBlank()) {
            idempotencyCache.put(key, response);
        }
    }
}
