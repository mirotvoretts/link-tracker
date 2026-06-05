package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.configuration.Grouping;
import backend.academy.linktracker.ai.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.kafka.ProcessedUpdateKafkaProducer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdateGroupingServiceTest {

    @Mock
    private AiAgentProperties properties;

    @Mock
    private Grouping grouping;

    @Mock
    private ProcessedUpdateKafkaProducer producer;

    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private UpdateGroupingService groupingService;

    @Captor
    private ArgumentCaptor<Runnable> flushTaskCaptor;

    @Captor
    private ArgumentCaptor<ProcessedUpdate> processedUpdateCaptor;

    @BeforeEach
    void setUp() {
        when(properties.getGrouping()).thenReturn(grouping);
        when(grouping.getWindowMs()).thenReturn(30000L);
        when(taskScheduler.schedule(flushTaskCaptor.capture(), any(Instant.class))).thenReturn(null);
    }

    @Test
    void submitGroupsMultipleUpdatesForSameChatIntoNumberedListWithMaxPriority() {
        ProcessedUpdate first =
                new ProcessedUpdate(1L, "First update", List.of(111L), UpdatePriority.LOW);
        ProcessedUpdate second =
                new ProcessedUpdate(2L, "Second update", List.of(111L), UpdatePriority.HIGH);

        groupingService.submit(first);
        groupingService.submit(second);
        flushTaskCaptor.getValue().run();

        verify(producer).send(processedUpdateCaptor.capture());
        ProcessedUpdate grouped = processedUpdateCaptor.getValue();

        assertThat(grouped.description()).isEqualTo("1. First update\n2. Second update");
        assertThat(grouped.priority()).isEqualTo(UpdatePriority.HIGH);
        assertThat(grouped.tgChatIds()).containsExactly(111L);
        assertThat(grouped.id()).isEqualTo(1L);
    }

    @Test
    void submitPassesThroughSingleUpdateWithoutGrouping() {
        ProcessedUpdate single =
                new ProcessedUpdate(5L, "Only update", List.of(222L), UpdatePriority.MEDIUM);

        groupingService.submit(single);
        flushTaskCaptor.getValue().run();

        verify(producer).send(processedUpdateCaptor.capture());
        ProcessedUpdate published = processedUpdateCaptor.getValue();

        assertThat(published).isEqualTo(single);
        assertThat(published.description()).doesNotContain("1.");
    }
}
