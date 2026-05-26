package com.karat.mockservices.model.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {
    private String productId;
    private boolean available;
    private int quantity;
    private String warehouseId;
    private String reservedUntil;   // non-null when someone else reserved it
    private String message;
}
