package com.karat.mockservices.model.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String notificationId;
    private String status;   // QUEUED, DROPPED
    private String channel;
    private String queuedAt;
    private String warning;  // hint: "Delivery not guaranteed"
}
