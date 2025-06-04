package eyeshead.smarthome.collector;

import eyeshead.smarthome.collector.kafka.config.CollectorKafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CollectorKafkaProducerConfig.class)
@Slf4j
public class Collector {
    public static void main(String[] args) {
        SpringApplication.run(Collector.class, args);
    }
}