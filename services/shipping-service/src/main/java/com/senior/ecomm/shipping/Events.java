package com.senior.ecomm.shipping;

import java.time.Instant;

record PaymentSucceededEvent(
    String eventType,
    String orderId,
    String paymentId,
    Instant createdAt
) {}

record ShipmentPreparedEvent(
    String eventType,
    String orderId,
    String shipmentId,
    Instant createdAt
) {}

