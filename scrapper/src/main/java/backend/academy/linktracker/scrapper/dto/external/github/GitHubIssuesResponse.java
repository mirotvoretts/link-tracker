package backend.academy.linktracker.scrapper.dto.external.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubIssuesResponse(List<GitHubIssue> items) {}
