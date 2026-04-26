package com.senior.ecomm.shipping;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class ShippingConsumer {
  private final KafkaTemplate<String, Object> kafka;
  private final String shippingTopic;

  ShippingConsumer(
      KafkaTemplate<String, Object> kafka,
      @Value("${app.kafka.topics.shippingEvents}") String shippingTopic
  ) {
    this.kafka = kafka;
    this.shippingTopic = shippingTopic;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.paymentEvents}",
      containerFactory = "paymentSucceededListenerFactory"
  )
  void onPaymentSucceeded(PaymentSucceededEvent event) {
    var shipmentId = UUID.randomUUID().toString();
    var prepared = new ShipmentPreparedEvent("SHIPMENT_PREPARED", event.orderId(), shipmentId, Instant.now());
    kafka.send(shippingTopic, event.orderId(), prepared);
  }
}

