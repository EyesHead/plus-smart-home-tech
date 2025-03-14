package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.kafka.config.AggregatorKafkaConsumerConfig;
import ru.yandex.practicum.kafka.config.AggregatorKafkaProducerConfig;

/**
 * Главный класс сервиса Aggregator.
 */
@SpringBootApplication
@EnableConfigurationProperties({
        AggregatorKafkaProducerConfig.class,
        AggregatorKafkaConsumerConfig.class
})
@Slf4j
public class Aggregator {

    public static void main(String[] args) {
        // Запуск Spring Boot приложения при помощи вспомогательного класса SpringApplication
        // метод run возвращает назад настроенный контекст, который мы можем использовать для
        // получения настроенных бинов
        ConfigurableApplicationContext context = SpringApplication.run(Aggregator.class, args);

        // Получаем бин AggregationStarter из контекста и запускаем основную логику сервиса
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);

        aggregator.start();
    }
}