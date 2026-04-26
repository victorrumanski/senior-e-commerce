package com.senior.ecomm.checkout;

import java.time.Instant;
import java.util.List;

record OrderItem(String sku, int quantity) {}

public record OrderPlacedEvent(
    String eventType,
    String orderId,
    String userId,
    List<OrderItem> items,
    String shippingAddress,
    String paymentMethodToken,
    Instant createdAt
) {}

