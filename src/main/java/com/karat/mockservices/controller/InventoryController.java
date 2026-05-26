package com.karat.mockservices.controller;

import com.karat.mockservices.model.inventory.*;
import com.karat.mockservices.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * GET /api/inventory/{productId}
     *
     * Scenario-based responses:
     * - PROD-IN-STOCK     → available: true, qty: 50
     * - PROD-OUT-STOCK    → available: false, qty: 0
     * - PROD-RACE         → first call true, second call false (race condition simulation)
     * - PROD-LOW-STOCK    → available: true, qty: 1 (edge case)
     * - anything else     → available: true, qty: 100
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> checkInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.checkInventory(productId));
    }

    /**
     * POST /api/inventory/deduct
     *
     * Request: { "productId": "PROD-101", "quantity": 2 }
     *
     * Scenarios:
     * - PROD-IN-STOCK     → success deduction
     * - PROD-OUT-STOCK    → throws 409 CONFLICT
     * - PROD-RACE         → 50% chance of failure (simulate concurrent deduction)
     * - PROD-SLOW         → takes 4 seconds (simulate slow inventory system)
     */
    @PostMapping("/deduct")
    public ResponseEntity<DeductResponse> deductStock(@RequestBody DeductRequest request) throws InterruptedException {
        return inventoryService.deductStock(request);
    }

    /**
     * POST /api/inventory/reserve
     *
     * Reserves stock for 5 minutes — proper pattern to avoid race conditions
     * Request: { "productId": "PROD-101", "quantity": 2, "reservationId": "RES-UUID" }
     */
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveStock(@RequestBody ReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(request));
    }

    /**
     * DELETE /api/inventory/reserve/{reservationId}
     * Releases a reservation (called on payment failure)
     */
    @DeleteMapping("/reserve/{reservationId}")
    public ResponseEntity<Void> releaseReservation(@PathVariable String reservationId) {
        inventoryService.releaseReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
