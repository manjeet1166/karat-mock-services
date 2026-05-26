package com.karat.mockservices.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeductResponse {
    private String productId;
    private int deducted;
    private int remainingStock;
    private String status; // DEDUCTED, FAILED
}
