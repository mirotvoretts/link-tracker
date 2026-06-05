package backend.academy.linktracker.scrapper.dto.external.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StackOverflowCommentResponse(List<StackOverflowComment> items) {}
