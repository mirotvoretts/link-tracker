package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.property.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public DefaultErrorHandler errorHandler(ConsumerRecordRecoverer deadLetterPublishingRecoverer) {
        ExponentialBackOff backOff =
                new ExponentialBackOff(kafkaProperties.getInitialBackoffMs(), kafkaProperties.getBackoffMultiplier());
        backOff.setMaxInterval(kafkaProperties.getMaxBackoffMs());

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);

        errorHandler.setRetryListeners((record, exception, retryCount) -> log.warn(
                "Retry attempt {} for message from topic: {}. Exception: {}",
                retryCount,
                record.topic(),
                exception.getClass().getSimpleName()));

        errorHandler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                IllegalArgumentException.class);

        return errorHandler;
    }

    @Bean
    public ConsumerRecordRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, ?> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate);
    }
}
