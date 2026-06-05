package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.kafka.ProcessedUpdateKafkaProducer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class UpdateGroupingService {

    private final AiAgentProperties properties;
    private final ProcessedUpdateKafkaProducer producer;
    private final TaskScheduler taskScheduler;
    private final ConcurrentMap<Long, ChatBuffer> buffers = new ConcurrentHashMap<>();

    public UpdateGroupingService(
            AiAgentProperties properties,
            ProcessedUpdateKafkaProducer producer,
            @Qualifier("groupingTaskScheduler") TaskScheduler taskScheduler) {
        this.properties = properties;
        this.producer = producer;
        this.taskScheduler = taskScheduler;
    }

    public void submit(ProcessedUpdate update) {
        for (Long chatId : update.tgChatIds()) {
            submitForChat(chatId, update);
        }
    }

    private void submitForChat(long chatId, ProcessedUpdate update) {
        PendingUpdate pending = new PendingUpdate(update.id(), update.description(), update.priority());
        buffers.compute(chatId, (id, buffer) -> {
            if (buffer == null) {
                buffer = new ChatBuffer();
                long windowMs = properties.getGrouping().getWindowMs();
                taskScheduler.schedule(() -> flush(id), Instant.now().plusMillis(windowMs));
            }
            buffer.add(pending);
            return buffer;
        });
    }

    private void flush(long chatId) {
        ChatBuffer buffer = buffers.remove(chatId);
        if (buffer == null || buffer.isEmpty()) {
            return;
        }
        producer.send(buildProcessedUpdate(chatId, buffer.entries()));
    }

    private ProcessedUpdate buildProcessedUpdate(long chatId, List<PendingUpdate> entries) {
        if (entries.size() == 1) {
            PendingUpdate entry = entries.getFirst();
            return new ProcessedUpdate(entry.id(), entry.description(), List.of(chatId), entry.priority());
        }

        String description = formatGroupedDescription(entries);
        String priority = entries.stream()
                .map(PendingUpdate::priority)
                .reduce(UpdatePriority::max)
                .orElse(UpdatePriority.MEDIUM);

        return new ProcessedUpdate(entries.getFirst().id(), description, List.of(chatId), priority);
    }

    private String formatGroupedDescription(List<PendingUpdate> entries) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < entries.size(); index++) {
            if (index > 0) {
                builder.append('\n');
            }
            builder.append(index + 1).append(". ").append(entries.get(index).description());
        }
        return builder.toString();
    }

    private static final class ChatBuffer {
        private final List<PendingUpdate> entries = new ArrayList<>();

        void add(PendingUpdate update) {
            entries.add(update);
        }

        boolean isEmpty() {
            return entries.isEmpty();
        }

        List<PendingUpdate> entries() {
            return List.copyOf(entries);
        }
    }

    private record PendingUpdate(long id, String description, String priority) {}
}
