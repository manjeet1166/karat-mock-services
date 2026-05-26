package com.karat.mockservices.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReservationResponse {
    private String reservationId;
    private String productId;
    private int quantity;
    private String status;      // RESERVED, FAILED
    private String expiresAt;   // ISO timestamp, 5 min from now
}
