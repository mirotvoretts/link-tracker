package backend.academy.linktracker.scrapper.dto.external.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowQuestion(
        @JsonProperty("question_id") long questionId,
        StackOverflowUser author,
        String title,
        @JsonProperty("last_activity_date") long lastActivityDate) {}
