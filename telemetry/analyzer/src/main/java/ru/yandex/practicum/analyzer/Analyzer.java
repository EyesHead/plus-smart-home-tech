package ru.yandex.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        SpringApplication.run(Analyzer.class, args);
    }
}