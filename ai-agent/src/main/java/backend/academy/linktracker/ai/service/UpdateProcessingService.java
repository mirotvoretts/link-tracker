package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.dto.RawUpdate;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UpdateProcessingService {
    private final UpdateFilteringService filteringService;
    private final UpdateSummarizationService summarizationService;
    private final UpdatePrioritizationService prioritizationService;

    public UpdateProcessingService(
            UpdateFilteringService filteringService,
            UpdateSummarizationService summarizationService,
            UpdatePrioritizationService prioritizationService) {
        this.filteringService = filteringService;
        this.summarizationService = summarizationService;
        this.prioritizationService = prioritizationService;
    }

    public Optional<ProcessedUpdate> process(RawUpdate rawUpdate) {
        return filteringService.filter(rawUpdate).map(this::summarizeAndTransform);
    }

    private ProcessedUpdate summarizeAndTransform(RawUpdate filtered) {
        String summarized = summarizationService.summarize(filtered.description());
        String priority = prioritizationService.prioritize(summarized);
        return new ProcessedUpdate(filtered.id(), summarized, filtered.tgChatIds(), priority);
    }
}
