package backend.academy.linktracker.scrapper.dto.external.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPullRequestsResponse(List<GitHubPullRequest> items) {}
