package com.karat.mockservices.service;

import com.karat.mockservices.model.inventory.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InventoryService {

    // Track call counts for race condition simulation
    private final Map<String, AtomicInteger> callCounter = new ConcurrentHashMap<>();
    // Track active reservations
    private final Map<String, ReservationResponse> reservations = new ConcurrentHashMap<>();

    private final Random random = new Random();

    public InventoryResponse checkInventory(String productId) {
        return switch (productId.toUpperCase()) {

            case "PROD-IN-STOCK" -> InventoryResponse.builder()
                    .productId(productId)
                    .available(true)
                    .quantity(50)
                    .warehouseId("WH-BLR-01")
                    .build();

            case "PROD-OUT-STOCK" -> InventoryResponse.builder()
                    .productId(productId)
                    .available(false)
                    .quantity(0)
                    .warehouseId("WH-BLR-01")
                    .reservedUntil("2099-01-15T10:30:00Z")
                    .message("Product currently out of stock")
                    .build();

            case "PROD-LOW-STOCK" -> InventoryResponse.builder()
                    .productId(productId)
                    .available(true)
                    .quantity(1) // only 1 left — race condition bait!
                    .warehouseId("WH-BLR-01")
                    .build();

            case "PROD-RACE" -> {
                // First call → available, second call → not available
                // Simulates two users hitting at same time
                int count = callCounter
                        .computeIfAbsent(productId, k -> new AtomicInteger(0))
                        .incrementAndGet();

                if (count % 2 == 1) {
                    yield InventoryResponse.builder()
                            .productId(productId)
                            .available(true)
                            .quantity(1)
                            .warehouseId("WH-BLR-01")
                            .message("Call #" + count + " — stock appears available")
                            .build();
                } else {
                    yield InventoryResponse.builder()
                            .productId(productId)
                            .available(false)
                            .quantity(0)
                            .warehouseId("WH-BLR-01")
                            .message("Call #" + count + " — stock was taken by concurrent request!")
                            .build();
                }
            }

            default -> InventoryResponse.builder()
                    .productId(productId)
                    .available(true)
                    .quantity(100)
                    .warehouseId("WH-BLR-01")
                    .build();
        };
    }

    public ResponseEntity<DeductResponse> deductStock(DeductRequest request) throws InterruptedException {

        String productId = request.getProductId().toUpperCase();

        // Slow service simulation
        if ("PROD-SLOW".equals(productId)) {
            Thread.sleep(4000); // 4 second delay
        }

        // Out of stock — conflict
        if ("PROD-OUT-STOCK".equals(productId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(DeductResponse.builder()
                            .productId(request.getProductId())
                            .status("FAILED")
                            .deducted(0)
                            .remainingStock(0)
                            .build());
        }

        // Race condition product — 50% fail
        if ("PROD-RACE".equals(productId) && random.nextBoolean()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(DeductResponse.builder()
                            .productId(request.getProductId())
                            .status("FAILED")
                            .deducted(0)
                            .remainingStock(0)
                            .build());
        }

        // Normal deduction
        return ResponseEntity.ok(DeductResponse.builder()
                .productId(request.getProductId())
                .deducted(request.getQuantity())
                .remainingStock(50 - request.getQuantity())
                .status("DEDUCTED")
                .build());
    }

    public ReservationResponse reserveStock(ReservationRequest request) {
        String expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES).toString();

        ReservationResponse response = ReservationResponse.builder()
                .reservationId(request.getReservationId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status("RESERVED")
                .expiresAt(expiresAt)
                .build();

        reservations.put(request.getReservationId(), response);
        return response;
    }

    public void releaseReservation(String reservationId) {
        reservations.remove(reservationId);
    }
}
