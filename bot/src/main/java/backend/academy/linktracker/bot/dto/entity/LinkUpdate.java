package backend.academy.linktracker.bot.dto.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LinkUpdate {
    long id;
    String url;
    String description;
    List<Long> tgChatIds;

    @JsonProperty("change_type")
    String changeType;

    String title;
    String username;

    @JsonProperty("created_at")
    OffsetDateTime createdAt;

    String preview;

    public LinkUpdate(long id, String url, String description, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.tgChatIds = tgChatIds;
    }
}
