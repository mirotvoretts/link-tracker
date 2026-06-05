package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.dto.RawUpdate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdateProcessingServiceTest {

    @Mock
    private UpdateFilteringService filteringService;

    @Mock
    private UpdateSummarizationService summarizationService;

    @Mock
    private UpdatePrioritizationService prioritizationService;

    @InjectMocks
    private UpdateProcessingService processingService;

    private RawUpdate validRawUpdate;

    @BeforeEach
    void setUp() {
        validRawUpdate = new RawUpdate(123L, "Original description", "author", List.of(111L, 222L));
    }

    @Test
    void processReturnsEmptyWhenFilteringRejectsUpdate() {
        when(filteringService.filter(validRawUpdate)).thenReturn(Optional.empty());

        Optional<ProcessedUpdate> result = processingService.process(validRawUpdate);

        assertThat(result).isEmpty();
    }

    @Test
    void processReturnsSummarizedAndPrioritizedUpdateWhenFilteringPasses() {
        String summarized = "Summarized text";
        when(filteringService.filter(validRawUpdate)).thenReturn(Optional.of(validRawUpdate));
        when(summarizationService.summarize("Original description")).thenReturn(summarized);
        when(prioritizationService.prioritize(summarized)).thenReturn(UpdatePriority.MEDIUM);

        Optional<ProcessedUpdate> result = processingService.process(validRawUpdate);

        assertThat(result).isPresent();
        ProcessedUpdate processed = result.get();
        assertThat(processed.id()).isEqualTo(123L);
        assertThat(processed.description()).isEqualTo(summarized);
        assertThat(processed.tgChatIds()).isEqualTo(List.of(111L, 222L));
        assertThat(processed.priority()).isEqualTo(UpdatePriority.MEDIUM);
    }

    @Test
    void processPreservesIdAndChatIdsAfterProcessing() {
        when(filteringService.filter(validRawUpdate)).thenReturn(Optional.of(validRawUpdate));
        when(summarizationService.summarize(any())).thenReturn("Summarized");
        when(prioritizationService.prioritize(any())).thenReturn(UpdatePriority.LOW);

        Optional<ProcessedUpdate> result = processingService.process(validRawUpdate);

        assertThat(result).hasValueSatisfying(update -> {
            assertThat(update.id()).isEqualTo(validRawUpdate.id());
            assertThat(update.tgChatIds()).isEqualTo(validRawUpdate.tgChatIds());
        });
    }
}
