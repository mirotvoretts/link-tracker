package backend.academy.linktracker.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record LinkUpdateRequest(
        long id,
        String url,
        String description,
        @JsonProperty("tg_chat_ids") List<Long> tgChatIds,
        @JsonProperty("change_details") LinkChangeDetails changeDetails) {

    public record LinkChangeDetails(
            @JsonProperty("change_type") String changeType,
            String title,
            String username,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            String preview) {}
}
