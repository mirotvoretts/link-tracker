package backend.academy.linktracker.ai.kafka;

import backend.academy.linktracker.ai.dto.ProcessedUpdate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProcessedUpdateKafkaProducer {

    private static final String TOPIC = "link.processed-updates";
    private final KafkaTemplate<String, ProcessedUpdate> kafkaTemplate;

    public ProcessedUpdateKafkaProducer(KafkaTemplate<String, ProcessedUpdate> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ProcessedUpdate processedUpdate) {
        kafkaTemplate.send(TOPIC, String.valueOf(processedUpdate.id()), processedUpdate);
    }
}
