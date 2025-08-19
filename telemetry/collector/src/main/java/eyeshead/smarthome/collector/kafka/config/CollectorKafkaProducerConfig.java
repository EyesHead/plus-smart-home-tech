package eyeshead.smarthome.collector.kafka.config;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@ConfigurationProperties("collector.kafka.producer")
@AllArgsConstructor
@Getter
@ToString
@Slf4j
public class CollectorKafkaProducerConfig {
    private final Map<String, String> properties;
    private final String hubTopic;
    private final String sensorTopic;

    public Properties getProperties() {
        Properties props = new Properties();
        if  (properties != null) {
            props.putAll(properties);
        }
        return props;
    }

    @PostConstruct
    public void logInitialization() {
        log.debug("Collector kafka producer конфиг: {}", this);
    }
}