package com.karat.mockservices.model.inventory;

import lombok.Data;

@Data
public class DeductRequest {
    private String productId;
    private int quantity;
    private String reservationId; // optional — proper pattern
}
