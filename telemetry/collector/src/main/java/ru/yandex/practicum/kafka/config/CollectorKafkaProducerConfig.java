package ru.yandex.practicum.kafka.config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@AllArgsConstructor
@ConfigurationProperties("collector.kafka-producer")
@Getter
public class CollectorKafkaProducerConfig {
    private final Map<String, String> properties;
    private final String hubTopic;
    private final String sensorTopic;

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}