package com.karat.mockservices.model.notification;

import lombok.Data;

@Data
public class NotificationRequest {
    private String userId;
    private String orderId;
    private String channel;  // EMAIL, SMS, PUSH
    private String message;  // optional custom message
}
