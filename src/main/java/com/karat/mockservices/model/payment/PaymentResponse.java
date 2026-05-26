package com.karat.mockservices.model.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private String transactionId;
    private String status;        // SUCCESS, PENDING, FAILED
    private double amount;
    private String processedAt;   // null if PENDING
    private String failureReason; // INSUFFICIENT_FUNDS, CARD_DECLINED, etc.
    private String message;
}
