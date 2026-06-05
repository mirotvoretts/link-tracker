package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.configuration.Prioritization;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdatePrioritizationServiceTest {

    @Mock
    private AiAgentProperties properties;

    @Mock
    private Prioritization prioritization;

    @InjectMocks
    private UpdatePrioritizationService prioritizationService;

    @BeforeEach
    void setUp() {
        when(properties.getPrioritization()).thenReturn(prioritization);
        when(prioritization.getHighKeywords()).thenReturn(List.of("critical", "urgent", "breaking", "security"));
        when(prioritization.getLowKeywords()).thenReturn(List.of("minor", "typo", "chore", "docs"));
    }

    @Test
    void prioritizeReturnsHighWhenTextContainsHighKeyword() {
        String result = prioritizationService.prioritize("critical bug fix");

        assertThat(result).isEqualTo(UpdatePriority.HIGH);
    }

    @Test
    void prioritizeReturnsMediumWhenTextContainsNoKeywords() {
        String result = prioritizationService.prioritize("regular repository update");

        assertThat(result).isEqualTo(UpdatePriority.MEDIUM);
    }

    @Test
    void prioritizeReturnsLowWhenTextContainsLowKeyword() {
        String result = prioritizationService.prioritize("fix typo in readme");

        assertThat(result).isEqualTo(UpdatePriority.LOW);
    }
}
