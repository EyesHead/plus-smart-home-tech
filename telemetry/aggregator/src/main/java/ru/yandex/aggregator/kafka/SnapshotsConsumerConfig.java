package ru.yandex.aggregator.kafka;

import org.apache.kafka.common.serialization.VoidDeserializer;

import java.util.Properties;
import java.util.UUID;

public class SnapshotsConsumerConfig {
    public static Properties init() {
        Properties config = new Properties();
        // эти настройки нужны, чтобы консьюмер всегда читал сообщения с самого начала топика (то есть все сообщения)
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        // обязательные настройки
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        config.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);

        return config;
    }
}
