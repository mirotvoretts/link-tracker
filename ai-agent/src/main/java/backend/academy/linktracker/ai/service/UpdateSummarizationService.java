package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.configuration.AiAgentProperties;
import org.springframework.stereotype.Service;

@Service
public class UpdateSummarizationService {

    private final AiAgentProperties properties;

    public UpdateSummarizationService(AiAgentProperties properties) {
        this.properties = properties;
    }

    public String summarize(String description) {
        int threshold = properties.getSummarization().getThreshold();

        if (description.length() <= threshold) {
            return description;
        }

        return description.substring(0, threshold) + "...";
    }
}
