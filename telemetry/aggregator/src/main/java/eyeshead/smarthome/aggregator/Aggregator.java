package eyeshead.smarthome.aggregator;

import eyeshead.smarthome.aggregator.kafka.config.AggregatorConsumerConfig;
import eyeshead.smarthome.aggregator.kafka.config.AggregatorProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({
        AggregatorConsumerConfig.class,
        AggregatorProducerConfig.class
})
public class Aggregator {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Aggregator.class, args);

        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();
    }
}