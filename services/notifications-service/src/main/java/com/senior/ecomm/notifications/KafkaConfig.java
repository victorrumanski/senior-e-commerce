package com.senior.ecomm.notifications;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
class KafkaConfig {
  @Bean
  ConcurrentKafkaListenerContainerFactory<String, DeliveredEvent> deliveredListenerFactory(
      @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "notifications-service");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    var json = new JsonDeserializer<>(DeliveredEvent.class);
    json.addTrustedPackages("com.senior.ecomm.*");
    var cf = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);

    var factory = new ConcurrentKafkaListenerContainerFactory<String, DeliveredEvent>();
    factory.setConsumerFactory(cf);
    return factory;
  }
}

