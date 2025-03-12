package ru.yandex.practicum.kafka.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("aggregator.kafka-consumer")
public class AggregatorKafkaConsumerConfig {
    private final Map<String, String> properties;
    @Getter
    private final String topic;

    public AggregatorKafkaConsumerConfig(Map<String, String> properties, String topic) {
        this.properties = properties != null ? properties : new HashMap<>();
        this.topic = topic;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}
