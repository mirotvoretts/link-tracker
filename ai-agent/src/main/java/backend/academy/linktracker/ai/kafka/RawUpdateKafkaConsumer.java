package backend.academy.linktracker.ai.kafka;

import backend.academy.linktracker.ai.dto.RawUpdate;
import backend.academy.linktracker.ai.service.UpdateGroupingService;
import backend.academy.linktracker.ai.service.UpdateProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RawUpdateKafkaConsumer {

    private final UpdateProcessingService processingService;
    private final UpdateGroupingService groupingService;

    public RawUpdateKafkaConsumer(UpdateProcessingService processingService, UpdateGroupingService groupingService) {
        this.processingService = processingService;
        this.groupingService = groupingService;
    }

    @KafkaListener(topics = "link.raw-updates", groupId = "ai-agent-consumer")
    public void consume(RawUpdate rawUpdate) {
        processingService.process(rawUpdate).ifPresent(groupingService::submit);
    }
}
