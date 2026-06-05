package backend.academy.linktracker.scrapper.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record LinkChangeDetails(
        @JsonProperty("change_type") String changeType,
        String title,
        String username,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        String preview) {

    private static String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    public static String truncate(String text) {
        return truncate(text, 200);
    }
}
