package com.senior.ecomm.inventory;

import java.time.Instant;

record PaymentSucceededEvent(
    String eventType,
    String orderId,
    String paymentId,
    Instant createdAt
) {}

