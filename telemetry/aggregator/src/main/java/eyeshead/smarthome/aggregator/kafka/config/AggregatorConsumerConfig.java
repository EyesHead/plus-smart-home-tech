package eyeshead.smarthome.aggregator.kafka.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("aggregator.kafka.consumer")
@RequiredArgsConstructor
@ToString
@Getter
@Slf4j
public class AggregatorConsumerConfig {
    private final Duration pollTimeout;
    private final String topic;
    private final Map<String, String> properties;

    public Properties getProperties() {
        Properties props = new Properties();
        if (properties != null) {
            props.putAll(properties);
        }
        return props;
    }

    @PostConstruct
    public void logInitialization() {
        log.debug("Aggregator kafka Consumer конфиг: {}", this);
    }
}