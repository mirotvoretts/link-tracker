package backend.academy.linktracker.scrapper.dto.request;

import backend.academy.linktracker.scrapper.dto.external.LinkChangeDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LinkUpdateRequest(
        long id,
        String url,
        String description,
        @JsonProperty("tg_chat_ids") List<Long> tgChatIds,
        @JsonProperty("change_details") LinkChangeDetails changeDetails) {}
