package com.karat.mockservices.model.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RefundResponse {
    private String refundId;
    private String transactionId;
    private double amount;
    private String status; // REFUNDED
    private String processedAt;
}
