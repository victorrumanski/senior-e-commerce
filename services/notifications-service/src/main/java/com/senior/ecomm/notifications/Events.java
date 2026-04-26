package com.senior.ecomm.notifications;

import java.time.Instant;

record DeliveredEvent(
    String eventType,
    String orderId,
    String deliveryId,
    double lat,
    double lon,
    Instant createdAt
) {}

