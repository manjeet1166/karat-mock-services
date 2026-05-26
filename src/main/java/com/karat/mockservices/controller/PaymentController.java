package com.karat.mockservices.controller;

import com.karat.mockservices.model.payment.*;
import com.karat.mockservices.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * POST /api/payments/charge
     *
     * Request: { "userId": "USR-001", "amount": 250.00, "idempotencyKey": "ORDER-UUID" }
     *
     * Scenario-based by AMOUNT:
     * - amount < 100        → { status: "SUCCESS", transactionId: "TXN-xxx" }
     * - amount 100–500      → { status: "PENDING" }  😈 ambiguous!
     * - amount > 500        → sleeps 5s then throws timeout  😈
     * - amount == 999       → { status: "FAILED", reason: "INSUFFICIENT_FUNDS" }
     * - amount == 503       → HTTP 503 Service Unavailable  😈
     *
     * Idempotency:
     * - Same idempotencyKey = same response (no double charge)
     */
    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(@RequestBody PaymentRequest request) throws InterruptedException {
        return paymentService.charge(request);
    }

    /**
     * GET /api/payments/status/{transactionId}
     *
     * Poll for PENDING transactions
     * - TXN-SETTLED   → SUCCESS
     * - TXN-FAILED    → FAILED
     * - TXN-PENDING   → still PENDING
     * - anything      → SUCCESS after 3 calls (simulates eventual settlement)
     */
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getStatus(transactionId));
    }

    /**
     * POST /api/payments/refund
     *
     * Request: { "transactionId": "TXN-xxx", "amount": 250.00 }
     * - Always returns SUCCESS (for saga compensation)
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> refund(@RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(request));
    }
}
