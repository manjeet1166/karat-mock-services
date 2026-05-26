package com.karat.mockservices.model.inventory;

import lombok.Data;

@Data
public class ReservationRequest {
    private String productId;
    private int quantity;
    private String reservationId;
}
