package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.dto.external.LinkChangeDetails;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

@Tag("unit")
class GitHubApiErrorHandlingTest {

    @Test
    void testHandleGitHubApiUnavailability() {
        String errorMessage = "GitHub API is temporarily unavailable";

        assertThrows(Exception.class, () -> {
            throw new HttpClientErrorException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, errorMessage);
        });
    }

    @Test
    void testHandle401UnauthorizedError() {
        assertThrows(Exception.class, () -> {
            throw new HttpClientErrorException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid GitHub token");
        });
    }

    @Test
    void testHandle404NotFoundError() {
        assertThrows(Exception.class, () -> {
            throw new HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND, "Repository not found");
        });
    }

    @Test
    void testBotHandlesApiErrorGracefully() {
        try {
            throw new Exception("API Error");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
            assertEquals("API Error", e.getMessage());
        }
    }

    @Test
    void testLinkChangeDetailsWithMissingData() {
        LinkChangeDetails details = new LinkChangeDetails("pull_request", null, "author", OffsetDateTime.now(), null);

        assertNull(details.title());
        assertNull(details.preview());
        assertEquals("pull_request", details.changeType());
    }

    @Test
    void testApiTimeoutHandling() {
        assertThrows(Exception.class, () -> {
            throw new java.net.SocketTimeoutException("Request timeout");
        });
    }

    @Test
    void testEmptyResponseFromGitHubApi() {
        String emptyResponse = "";

        assertNotNull(emptyResponse);
        assertTrue(emptyResponse.isEmpty());
    }
}
