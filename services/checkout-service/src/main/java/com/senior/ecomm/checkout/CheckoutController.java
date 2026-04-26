package com.senior.ecomm.checkout;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CheckoutController {
  private final KafkaTemplate<String, Object> kafka;
  private final String topic;

  CheckoutController(
      KafkaTemplate<String, Object> kafka,
      @Value("${app.kafka.topics.orderEvents}") String topic
  ) {
    this.kafka = kafka;
    this.topic = topic;
  }

  record PlaceOrderRequest(
      String userId,
      List<OrderItem> items,
      String shippingAddress,
      String paymentMethodToken
  ) {}

  @PostMapping("/orders")
  Map<String, Object> placeOrder(@RequestBody PlaceOrderRequest req) {
    var orderId = UUID.randomUUID().toString();
    var event = new OrderPlacedEvent(
        "ORDER_PLACED",
        orderId,
        req.userId(),
        req.items(),
        req.shippingAddress(),
        req.paymentMethodToken(),
        Instant.now()
    );

    kafka.send(topic, orderId, event);

    return Map.of(
        "orderId", orderId,
        "eventType", "ORDER_PLACED",
        "topic", topic
    );
  }
}

