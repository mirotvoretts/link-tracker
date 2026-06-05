package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.util.MdcUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.notifier", name = "type", havingValue = "kafka")
@RequiredArgsConstructor
public class KafkaNotificationService implements NotificationService {

    private static final String TOPIC = "link-updates";

    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Override
    public void sendUpdate(LinkUpdateRequest updateRequest) {
        try {
            MdcUtil.putUrl(updateRequest.url());
            MdcUtil.putLinkId(updateRequest.id());

            log.info("Sending notification via Kafka to topic: {}", TOPIC);

            kafkaTemplate
                    .send(TOPIC, String.valueOf(updateRequest.id()), updateRequest)
                    .whenComplete((result, exception) -> {
                        if (exception == null) {
                            log.info(
                                    "Notification sent successfully via Kafka, partition: {}, offset: {}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send notification via Kafka", exception);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending notification via Kafka", e);
        } finally {
            MdcUtil.clearUrl();
            MdcUtil.clearLinkId();
        }
    }

    @Override
    public String getName() {
        return "Kafka";
    }
}
