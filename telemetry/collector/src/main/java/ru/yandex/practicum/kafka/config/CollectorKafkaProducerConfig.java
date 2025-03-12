package ru.yandex.practicum.kafka.config;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("collector.kafka-producer")
public class CollectorKafkaProducerConfig {
    private final Map<String, String> properties;
    @Getter
    private final String hubTopic;
    @Getter
    private final String sensorTopic;

    public CollectorKafkaProducerConfig(Map<String, String> properties, String hubTopic, String sensorTopic) {
        this.properties = properties != null ? properties : new HashMap<>();
        this.hubTopic = hubTopic;
        this.sensorTopic = sensorTopic;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}