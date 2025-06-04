package eyeshead.smarthome.analyzer.hubevent.kafka.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("analyzer.kafka.hub-event-consumer")
@AllArgsConstructor
@Getter
@Slf4j
@ToString
public class HubEventConsumerConfig {
    private final Map<String, String> properties;
    private final String topic;
    private final Duration timeout;

    public Properties getProperties() {
        Properties props = new Properties();
        if  (properties != null) {
            props.putAll(properties);
        }
        return props;
    }

    @PostConstruct
    public void init() {
        log.debug("Analyzer kafka hubEvent consumer конфиг: {}", this);
    }
}
