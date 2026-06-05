package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.dto.external.LinkChangeDetails;
import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.property.BotProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class BotClient {
    private final RestClient restClient;

    public BotClient(BotProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    public void sendUpdate(LinkUpdateRequest updateRequest) {
        log.atInfo()
                .addKeyValue("url", updateRequest.url())
                .addKeyValue("chatIds", updateRequest.tgChatIds())
                .addKeyValue("description", updateRequest.description())
                .log("Sending link update to bot");

        try {
            LinkChangeDetails changeDetails = updateRequest.changeDetails();
            if (changeDetails == null) {
                log.warn("No change details provided in update request");
                return;
            }

            LinkUpdateDto notification = new LinkUpdateDto(
                    updateRequest.id(),
                    updateRequest.url(),
                    updateRequest.description(),
                    updateRequest.tgChatIds(),
                    changeDetails.changeType(),
                    changeDetails.title(),
                    changeDetails.username(),
                    changeDetails.createdAt(),
                    changeDetails.preview());

            restClient
                    .post()
                    .uri("/updates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(notification)
                    .retrieve()
                    .toBodilessEntity();

            log.atInfo()
                    .addKeyValue("url", updateRequest.url())
                    .addKeyValue("chatIds", updateRequest.tgChatIds())
                    .log("Update successfully sent to bot");
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("url", updateRequest.url())
                    .addKeyValue("chatIds", updateRequest.tgChatIds())
                    .addKeyValue("error", e.getMessage())
                    .addKeyValue("errorClass", e.getClass().getSimpleName())
                    .log("Failed to send update to bot");
            throw new RuntimeException("Failed to send update to bot", e);
        }
    }

    public record LinkUpdateDto(
            long id,
            String url,
            String description,
            List<Long> tgChatIds,
            @JsonProperty("change_type") String changeType,
            String title,
            String username,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            String preview) {}
}
