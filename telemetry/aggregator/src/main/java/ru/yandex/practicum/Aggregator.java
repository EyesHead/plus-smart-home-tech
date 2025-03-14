package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.yandex.practicum.kafka.config.AggregatorKafkaConsumerConfig;
import ru.yandex.practicum.kafka.config.AggregatorKafkaProducerConfig;

/**
 * Главный класс сервиса Aggregator.
 */
@EnableConfigurationProperties({
        AggregatorKafkaProducerConfig.class,
        AggregatorKafkaConsumerConfig.class
})
@Slf4j
@SpringBootApplication
public class Aggregator {
    public static void main(String[] args) {
        SpringApplication.run(Aggregator.class, args);
    }
}