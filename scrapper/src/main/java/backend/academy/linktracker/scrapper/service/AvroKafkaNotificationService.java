package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.notifier", name = "type", havingValue = "avro-kafka")
@RequiredArgsConstructor
public class AvroKafkaNotificationService implements NotificationService {

    private static final String TOPIC = "link-updates";

    private final KafkaTemplate<String, Object> avroKafkaTemplate;

    public void sendAvroUpdate(long linkId, String url, String description, List<Long> chatIds) {
        try {
            LinkUpdateEvent event = new LinkUpdateEvent(linkId, url, description, chatIds);

            log.info("Sending Avro notification to Kafka topic: {}, linkId: {}", TOPIC, linkId);

            avroKafkaTemplate.send(TOPIC, String.valueOf(linkId), event).whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info(
                            "Avro notification sent successfully, partition: {}, offset: {}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send Avro notification", exception);
                }
            });
        } catch (Exception e) {
            log.error("Error sending Avro notification", e);
            throw new RuntimeException("Failed to send Avro notification", e);
        }
    }

    @Override
    public void sendUpdate(LinkUpdateRequest updateRequest) {
        sendAvroUpdate(updateRequest.id(), updateRequest.url(), updateRequest.description(), updateRequest.tgChatIds());
    }

    @Override
    public String getName() {
        return "Avro-Kafka";
    }
}
