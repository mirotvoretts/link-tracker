package backend.academy.linktracker.bot.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeadLetterQueueListener {

    private static final String DLQ_TOPIC = "link-updates.DLT";

    @KafkaListener(topics = DLQ_TOPIC, groupId = "${spring.kafka.consumer.group-id}.dlq")
    public void listen(ConsumerRecord<String, String> record) {
        log.error(
                "Message received in Dead Letter Queue. Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value());
    }
}
