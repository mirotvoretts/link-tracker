package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import backend.academy.linktracker.ai.configuration.Prioritization;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UpdatePrioritizationService {

    private final AiAgentProperties properties;

    public UpdatePrioritizationService(AiAgentProperties properties) {
        this.properties = properties;
    }

    public String prioritize(String description) {
        Prioritization prioritization = properties.getPrioritization();
        String lowerText = description.toLowerCase();

        if (containsKeyword(lowerText, prioritization.getHighKeywords())) {
            return UpdatePriority.HIGH;
        }

        if (containsKeyword(lowerText, prioritization.getLowKeywords())) {
            return UpdatePriority.LOW;
        }

        return UpdatePriority.MEDIUM;
    }

    private boolean containsKeyword(String lowerText, List<String> keywords) {
        return keywords.stream().anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));
    }
}
