package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.kafka.config.CollectorKafkaProducerConfig;

@SpringBootApplication
@EnableConfigurationProperties(CollectorKafkaProducerConfig.class)
@Slf4j
public class CollectorApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CollectorApplication.class, args);
        CollectorKafkaProducerConfig config = context.getBean(CollectorKafkaProducerConfig.class);
        log.info("Producer: {}", config.getProperties());
        log.info("Topics: hubTopic: {}, sensorTopic: {}", config.getHubTopic(), config.getSensorTopic());
    }
}