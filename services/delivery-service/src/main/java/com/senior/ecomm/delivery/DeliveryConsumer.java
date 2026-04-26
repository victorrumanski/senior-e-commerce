package com.senior.ecomm.delivery;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class DeliveryConsumer {
  private final KafkaTemplate<String, Object> kafka;
  private final String deliveryTopic;

  DeliveryConsumer(
      KafkaTemplate<String, Object> kafka,
      @Value("${app.kafka.topics.deliveryEvents}") String deliveryTopic
  ) {
    this.kafka = kafka;
    this.deliveryTopic = deliveryTopic;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.shippingEvents}",
      containerFactory = "shipmentPreparedListenerFactory"
  )
  void onShipmentPrepared(ShipmentPreparedEvent event) {
    // teaching demo: "GPS tracking" is just a fixed coordinate
    var deliveryId = UUID.randomUUID().toString();
    var delivered = new DeliveredEvent("DELIVERED", event.orderId(), deliveryId, -23.5505, -46.6333, Instant.now());
    kafka.send(deliveryTopic, event.orderId(), delivered);
  }
}

