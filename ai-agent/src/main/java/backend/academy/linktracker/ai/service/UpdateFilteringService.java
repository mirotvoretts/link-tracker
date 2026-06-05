package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.configuration.Filtering;
import backend.academy.linktracker.ai.dto.RawUpdate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UpdateFilteringService {

    private final AiAgentProperties properties;

    public UpdateFilteringService(AiAgentProperties properties) {
        this.properties = properties;
    }

    public Optional<RawUpdate> filter(RawUpdate update) {
        Filtering filtering = properties.getFiltering();

        if (update.description().length() < filtering.getMinLength()) {
            return Optional.empty();
        }

        if (containsStopWord(update.description(), filtering.getStopWords())) {
            return Optional.empty();
        }

        if (filtering.getExcludedAuthors().contains(update.author())) {
            return Optional.empty();
        }

        return Optional.of(update);
    }

    private boolean containsStopWord(String text, List<String> stopWords) {
        String lowerText = text.toLowerCase();
        return stopWords.stream().anyMatch(word -> lowerText.contains(word.toLowerCase()));
    }
}
