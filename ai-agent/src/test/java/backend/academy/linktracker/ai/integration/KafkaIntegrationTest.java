package backend.academy.linktracker.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import backend.academy.linktracker.ai.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.dto.RawUpdate;
import backend.academy.linktracker.ai.service.UpdatePriority;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

@Tag("integration")
@SpringBootTest
@TestPropertySource(
        properties = {
            "ai-agent.filtering.stop-words=spam,ads",
            "ai-agent.filtering.excluded-authors=bot-user",
            "ai-agent.filtering.min-length=5",
            "ai-agent.summarization.threshold=30",
            "ai-agent.grouping.window-ms=200"
        })
class KafkaIntegrationTest extends KafkaIntegrationTestBase {

    @Autowired
    private KafkaTemplate<String, RawUpdate> rawUpdateTemplate;

    @Autowired
    private ProcessedUpdateCaptor captor;

    @BeforeEach
    void setUp() {
        captor.reset();
    }

    @Test
    void shouldPublishProcessedMessageToKafkaTopic() {
        RawUpdate rawUpdate =
                new RawUpdate(1L, "critical bug fix deployed", "normal-author", List.of(111L));

        rawUpdateTemplate.send("link.raw-updates", String.valueOf(rawUpdate.id()), rawUpdate);

        await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).hasSize(1));

        ProcessedUpdate published = captor.getCapturedUpdates().getFirst();
        assertThat(published.id()).isEqualTo(1L);
        assertThat(published.tgChatIds()).containsExactly(111L);
        assertThat(published.priority()).isEqualTo(UpdatePriority.HIGH);
        assertThat(published.description()).contains("critical");
    }

    @Test
    void shouldNotPublishFilteredMessageToOutputTopic() {
        RawUpdate filteredUpdate = new RawUpdate(2L, "This is spam content", "author", List.of(111L));

        rawUpdateTemplate.send("link.raw-updates", String.valueOf(filteredUpdate.id()), filteredUpdate);

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).isEmpty());
    }

    @Test
    void shouldReceiveAndProcessValidMessageWithMediumPriority() {
        RawUpdate rawUpdate = new RawUpdate(3L, "Valid description text", "normal-author", List.of(111L));

        rawUpdateTemplate.send("link.raw-updates", String.valueOf(rawUpdate.id()), rawUpdate);

        await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).hasSize(1));

        ProcessedUpdate published = captor.getCapturedUpdates().getFirst();
        assertThat(published.id()).isEqualTo(3L);
        assertThat(published.tgChatIds()).containsExactly(111L);
        assertThat(published.priority()).isEqualTo(UpdatePriority.MEDIUM);
    }

    @Test
    void shouldFilterOutMessageWithStopWord() {
        RawUpdate rawUpdateWithStopWord = new RawUpdate(4L, "This is spam content", "author", List.of(111L));

        rawUpdateTemplate.send("link.raw-updates", String.valueOf(rawUpdateWithStopWord.id()), rawUpdateWithStopWord);

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).isEmpty());
    }

    @Test
    void shouldFilterOutMessageFromExcludedAuthor() {
        RawUpdate rawUpdateFromExcludedAuthor = new RawUpdate(5L, "Valid description text", "bot-user", List.of(111L));

        rawUpdateTemplate.send(
                "link.raw-updates", String.valueOf(rawUpdateFromExcludedAuthor.id()), rawUpdateFromExcludedAuthor);

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).isEmpty());
    }

    @Test
    void shouldFilterOutShortMessage() {
        RawUpdate shortUpdate = new RawUpdate(6L, "abc", "author", List.of(111L));

        rawUpdateTemplate.send("link.raw-updates", String.valueOf(shortUpdate.id()), shortUpdate);

        await().pollDelay(500, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).isEmpty());
    }

    @Test
    void shouldSummarizeLongMessage() {
        String longDescription = "a".repeat(50);
        RawUpdate longUpdate = new RawUpdate(7L, longDescription, "author", List.of(111L));

        rawUpdateTemplate.send("link.raw-updates", String.valueOf(longUpdate.id()), longUpdate);

        await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(300, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(captor.getCapturedUpdates()).hasSize(1));

        assertThat(captor.getCapturedUpdates().getFirst().description()).isEqualTo("a".repeat(30) + "...");
    }

    @Component
    public static class ProcessedUpdateCaptor {
        private final List<ProcessedUpdate> capturedUpdates = new CopyOnWriteArrayList<>();

        void reset() {
            capturedUpdates.clear();
        }

        @KafkaListener(topics = "link.processed-updates", groupId = "ai-agent-test-captor")
        void capture(ProcessedUpdate update) {
            capturedUpdates.add(update);
        }

        List<ProcessedUpdate> getCapturedUpdates() {
            return List.copyOf(capturedUpdates);
        }
    }
}
