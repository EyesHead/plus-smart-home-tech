package ru.yandex.practicum.aggregator.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;
import java.util.Properties;

@Getter
@ConfigurationProperties("aggregator.kafka-consumer")
@AllArgsConstructor
public class AggregatorKafkaConsumerConfig {
    private final Map<String, String> properties;
    private final String topic;

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}