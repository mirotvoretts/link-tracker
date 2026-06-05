package backend.academy.linktracker.bot.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProperties {
    private int maxRetries = 3;
    private long initialBackoffMs = 1000;
    private long maxBackoffMs = 60000;
    private double backoffMultiplier = 2.0;
}
