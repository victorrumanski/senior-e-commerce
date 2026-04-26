package com.senior.ecomm.delivery;

import java.time.Instant;

record ShipmentPreparedEvent(
    String eventType,
    String orderId,
    String shipmentId,
    Instant createdAt
) {}

record DeliveredEvent(
    String eventType,
    String orderId,
    String deliveryId,
    double lat,
    double lon,
    Instant createdAt
) {}

