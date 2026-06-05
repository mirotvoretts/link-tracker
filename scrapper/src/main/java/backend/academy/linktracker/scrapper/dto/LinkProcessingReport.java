package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record LinkProcessingReport(
        int totalLinks, int successfulLinks, int failedLinks, List<LinkProcessingError> errors) {

    public record LinkProcessingError(long linkId, String url, long chatId, String errorMessage, String errorType) {}

    public static LinkProcessingReport success(int totalLinks) {
        return new LinkProcessingReport(totalLinks, totalLinks, 0, List.of());
    }
}
