package ru.yandex.practicum.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.aggregator.kafka.config.AggregatorKafkaConsumerConfig;
import ru.yandex.practicum.aggregator.kafka.config.AggregatorKafkaProducerConfig;

/**
 * Главный класс сервиса Aggregator.
 */

@EnableConfigurationProperties({
        AggregatorKafkaProducerConfig.class,
        AggregatorKafkaConsumerConfig.class
})
@Slf4j
@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.aggregator",
        "ru.yandex.practicum.collector.kafka.serializer"
})
@Import({
        ru.yandex.practicum.collector.kafka.serializer.BaseAvroSerializer.class
})
public class Aggregator {

    public static void main(String[] args) {
        // Запуск Spring Boot приложения при помощи вспомогательного класса SpringApplication
        // метод run возвращает назад настроенный контекст, который мы можем использовать для
        // получения настроенных бинов
        ConfigurableApplicationContext context = SpringApplication.run(Aggregator.class, args);

        AggregatorKafkaConsumerConfig consumerConfig = context.getBean(AggregatorKafkaConsumerConfig.class);
        AggregatorKafkaProducerConfig producerConfig = context.getBean(AggregatorKafkaProducerConfig.class);
        log.info("Consumer: {}", consumerConfig.getProperties());
        log.info("Producer: {}", producerConfig.getProperties());

        // Получаем бин AggregationStarter из контекста и запускаем основную логику сервиса
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);

        aggregator.start();
    }
}