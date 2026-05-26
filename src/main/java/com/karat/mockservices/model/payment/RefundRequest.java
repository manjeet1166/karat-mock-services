package com.karat.mockservices.model.payment;

import lombok.Data;

@Data
public class RefundRequest {
    private String transactionId;
    private double amount;
    private String reason;
}
