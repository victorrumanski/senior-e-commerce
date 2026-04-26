package com.senior.ecomm.inventory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class InventoryConsumer {
  @KafkaListener(
      topics = "${app.kafka.topics.paymentEvents}",
      containerFactory = "paymentSucceededListenerFactory"
  )
  void onPaymentSucceeded(PaymentSucceededEvent event) {
    // teaching demo: pretend we reserved stock earlier and now commit + clear reservations
    // (persisting inventory movements is the next exercise)
  }
}

