package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.external.github.GitHubIssue;
import backend.academy.linktracker.scrapper.dto.external.github.GitHubPullRequest;
import backend.academy.linktracker.scrapper.dto.external.github.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.property.GithubProperties;
import backend.academy.linktracker.scrapper.util.MdcUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubClient {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private final RestClient restClient;

    public GitHubClient(GithubProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(GITHUB_API_URL)
                .defaultHeader("Authorization", "Bearer " + properties.getToken())
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    public GitHubRepoResponse fetchRepository(String owner, String repo) {
        try {
            MdcUtil.putOwner(owner);
            MdcUtil.putRepo(repo);
            log.info("Fetching GitHub repository info");

            return restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(GitHubRepoResponse.class);
        } finally {
            MdcUtil.clearOwner();
            MdcUtil.clearRepo();
        }
    }

    public List<GitHubPullRequest> fetchPullRequests(String owner, String repo) {
        try {
            MdcUtil.putOwner(owner);
            MdcUtil.putRepo(repo);
            log.info("Fetching GitHub pull requests");

            return restClient
                    .get()
                    .uri("/repos/{owner}/{repo}/pulls?state=all&per_page=10&sort=updated", owner, repo)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } finally {
            MdcUtil.clearOwner();
            MdcUtil.clearRepo();
        }
    }

    public List<GitHubIssue> fetchIssues(String owner, String repo) {
        try {
            MdcUtil.putOwner(owner);
            MdcUtil.putRepo(repo);
            log.info("Fetching GitHub issues");

            return restClient
                    .get()
                    .uri("/repos/{owner}/{repo}/issues?state=all&per_page=10&sort=updated", owner, repo)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } finally {
            MdcUtil.clearOwner();
            MdcUtil.clearRepo();
        }
    }
}
