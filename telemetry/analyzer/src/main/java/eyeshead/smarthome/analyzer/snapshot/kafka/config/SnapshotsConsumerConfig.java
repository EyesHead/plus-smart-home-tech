package eyeshead.smarthome.analyzer.snapshot.kafka.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("analyzer.kafka.snapshot-consumer")
@AllArgsConstructor
@Getter
@Slf4j
@ToString
public class SnapshotsConsumerConfig {
    private final Map<String, String> properties;
    private final String topic;
    private final Duration pollTimeout;

    public Properties getProperties() {
        Properties props = new Properties();
        if  (properties != null) {
            props.putAll(properties);
        }
        return props;
    }

    @PostConstruct
    public void logInitialization() {
        log.debug("Analyzer kafka Snapshot consumer конфиг: {}", this);
    }
}
