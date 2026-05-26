package com.karat.mockservices.model.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private double amount;
    private String idempotencyKey; // should be unique per order
    private String currency;       // default USD
}
