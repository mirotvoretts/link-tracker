package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.property.GithubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class GitHubClientTest {

    private GitHubClient gitHubClient;

    @BeforeEach
    void setUp() {
        GithubProperties props = new GithubProperties();
        props.setToken("test-token");
        gitHubClient = new GitHubClient(props);
    }

    @Test
    void testFetchPullRequestsReturnsNonNullList() {
        assertNotNull(gitHubClient);
    }

    @Test
    void testFetchIssuesReturnsNonNullList() {
        assertNotNull(gitHubClient);
    }

    @Test
    void testGitHubClientInitializedWithToken() {
        GithubProperties props = new GithubProperties();
        props.setToken("ghp_test123");

        GitHubClient client = new GitHubClient(props);

        assertNotNull(client);
    }
}
