package com.senior.ecomm.payments;

import java.time.Instant;
import java.util.List;

record OrderItem(String sku, int quantity) {}

record OrderPlacedEvent(
    String eventType,
    String orderId,
    String userId,
    List<OrderItem> items,
    String shippingAddress,
    String paymentMethodToken,
    Instant createdAt
) {}

record PaymentSucceededEvent(
    String eventType,
    String orderId,
    String paymentId,
    Instant createdAt
) {}

