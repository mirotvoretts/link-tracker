package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.configuration.Filtering;
import backend.academy.linktracker.ai.dto.RawUpdate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateFilteringServiceTest {

    @Mock
    private AiAgentProperties properties;

    @Mock
    private Filtering filtering;

    @InjectMocks
    private UpdateFilteringService filteringService;

    private RawUpdate validUpdate;

    @BeforeEach
    void setUp() {
        validUpdate = new RawUpdate(1L, "Valid update description", "author", List.of(111L, 222L));
        when(properties.getFiltering()).thenReturn(filtering);
        when(filtering.getMinLength()).thenReturn(0);
        when(filtering.getStopWords()).thenReturn(List.of());
        when(filtering.getExcludedAuthors()).thenReturn(List.of());
    }

    @Test
    void filterReturnsEmptyWhenDescriptionLessThanMinLength() {
        when(filtering.getMinLength()).thenReturn(100);

        Optional<RawUpdate> result = filteringService.filter(validUpdate);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"spam", "ads", "promo"})
    void filterReturnsEmptyWhenDescriptionContainsStopWord(String stopWord) {
        RawUpdate updateWithStopWord = new RawUpdate(2L, "This is " + stopWord + " content", "author", List.of(111L));
        when(filtering.getStopWords()).thenReturn(List.of("spam", "ads", "promo"));

        Optional<RawUpdate> result = filteringService.filter(updateWithStopWord);

        assertThat(result).isEmpty();
    }

    @Test
    void filterReturnsEmptyWhenStopWordIsCaseInsensitive() {
        RawUpdate updateWithUpperStopWord = new RawUpdate(3L, "This contains SPAM", "author", List.of(111L));
        when(filtering.getStopWords()).thenReturn(List.of("spam"));

        Optional<RawUpdate> result = filteringService.filter(updateWithUpperStopWord);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"bot-user", "admin-bot", "service-account"})
    void filterReturnsEmptyWhenAuthorIsExcluded(String excludedAuthor) {
        RawUpdate updateFromExcludedAuthor = new RawUpdate(4L, "Some update", excludedAuthor, List.of(111L));
        when(filtering.getExcludedAuthors()).thenReturn(List.of("bot-user", "admin-bot", "service-account"));

        Optional<RawUpdate> result = filteringService.filter(updateFromExcludedAuthor);

        assertThat(result).isEmpty();
    }

    @Test
    void filterReturnsUpdateWhenAllFiltersPass() {
        Optional<RawUpdate> result = filteringService.filter(validUpdate);

        assertThat(result).contains(validUpdate);
    }

    @Test
    void filterAppliesMultipleFiltersInOrder() {
        RawUpdate updateFailingMultipleFilters = new RawUpdate(5L, "a", "bot-user", List.of(111L));
        when(filtering.getMinLength()).thenReturn(5);
        when(filtering.getExcludedAuthors()).thenReturn(List.of("bot-user"));

        Optional<RawUpdate> result = filteringService.filter(updateFailingMultipleFilters);

        assertThat(result).isEmpty();
    }
}
