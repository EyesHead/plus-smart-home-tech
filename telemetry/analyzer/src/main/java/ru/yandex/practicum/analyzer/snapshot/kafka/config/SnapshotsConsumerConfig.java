package ru.yandex.practicum.analyzer.snapshot.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("analyzer.snapshot-consumer")
@Getter
@AllArgsConstructor
public class SnapshotsConsumerConfig {
    private Map<String, String> properties;
    private String topic;

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}
