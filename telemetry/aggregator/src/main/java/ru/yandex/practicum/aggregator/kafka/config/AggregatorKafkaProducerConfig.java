package ru.yandex.practicum.aggregator.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;
import java.util.Properties;

@Getter
@ConfigurationProperties("aggregator.kafka-producer")
@AllArgsConstructor
public class AggregatorKafkaProducerConfig {
    private final String topic;
    private final Map<String, String> properties;

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}