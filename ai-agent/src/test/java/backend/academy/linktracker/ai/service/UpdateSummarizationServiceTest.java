package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.configuration.Summarization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdateSummarizationServiceTest {

    @Mock
    private AiAgentProperties properties;

    @Mock
    private Summarization summarization;

    @InjectMocks
    private UpdateSummarizationService summarizationService;

    @BeforeEach
    void setUp() {
        when(properties.getSummarization()).thenReturn(summarization);
        when(summarization.getThreshold()).thenReturn(50);
    }

    @Test
    void summarizeReturnsTextAsIsWhenLengthEqualsThreshold() {
        String textAtThreshold = "a".repeat(50);

        String result = summarizationService.summarize(textAtThreshold);

        assertThat(result).isEqualTo(textAtThreshold);
    }

    @Test
    void summarizeReturnsTextAsIsWhenLengthBelowThreshold() {
        String shortText = "Short text";

        String result = summarizationService.summarize(shortText);

        assertThat(result).isEqualTo(shortText);
    }

    @Test
    void summarizeTruncatesAndAppendsDotsWhenLengthExceedsThreshold() {
        String longText = "a".repeat(100);
        String expected = "a".repeat(50) + "...";

        String result = summarizationService.summarize(longText);

        assertThat(result).isEqualTo(expected);
        assertThat(result).endsWith("...");
        assertThat(result).hasSize(53);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 500, 1000})
    void summarizeRespectsDynamicThreshold(int threshold) {
        when(summarization.getThreshold()).thenReturn(threshold);
        String longText = "x".repeat(threshold + 50);

        String result = summarizationService.summarize(longText);

        assertThat(result).hasSize(threshold + 3);
        assertThat(result).endsWith("...");
    }

    @Test
    void summarizeHandlesEmptyString() {
        String result = summarizationService.summarize("");

        assertThat(result).isEmpty();
    }
}
