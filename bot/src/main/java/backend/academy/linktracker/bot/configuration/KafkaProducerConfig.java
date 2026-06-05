package backend.academy.linktracker.bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, ?> kafkaTemplate(ProducerFactory<String, ?> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
