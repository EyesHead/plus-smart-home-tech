package ru.yandex.practicum.analyzer.hub.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@AllArgsConstructor
@ConfigurationProperties("analyzer.hubs-consumer")
@Getter
public class HubEventConsumerConfig {
    private final Map<String, String> properties;
    private final String topic;

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }
}
