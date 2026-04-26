package com.senior.ecomm.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class NotificationConsumer {
  private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

  @KafkaListener(
      topics = "${app.kafka.topics.deliveryEvents}",
      containerFactory = "deliveredListenerFactory"
  )
  void onDelivered(DeliveredEvent event) {
    log.info("Notify user: order {} delivered at ({}, {})", event.orderId(), event.lat(), event.lon());
  }
}

