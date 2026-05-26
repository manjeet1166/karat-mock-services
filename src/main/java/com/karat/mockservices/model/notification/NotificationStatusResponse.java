package com.karat.mockservices.model.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationStatusResponse {
    private String notificationId;
    private String status;      // DELIVERED, FAILED, PENDING
    private String deliveredAt;
    private String failureReason;
}
