package com.senior.ecomm.payments;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class PaymentConsumer {
  private final KafkaTemplate<String, Object> kafka;
  private final String paymentTopic;

  PaymentConsumer(
      KafkaTemplate<String, Object> kafka,
      @Value("${app.kafka.topics.paymentEvents}") String paymentTopic
  ) {
    this.kafka = kafka;
    this.paymentTopic = paymentTopic;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.orderEvents}",
      containerFactory = "orderPlacedListenerFactory"
  )
  void onOrderPlaced(OrderPlacedEvent event) {
    // teaching demo: assume payment always succeeds
    var paymentId = UUID.randomUUID().toString();
    var succeeded = new PaymentSucceededEvent("PAYMENT_SUCCEEDED", event.orderId(), paymentId, Instant.now());
    kafka.send(paymentTopic, event.orderId(), succeeded);
  }
}

