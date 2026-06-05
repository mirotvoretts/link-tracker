package backend.academy.linktracker.bot.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.dto.entity.LinkUpdate;
import backend.academy.linktracker.bot.dto.response.ResponseBodyLiterals;
import backend.academy.linktracker.bot.service.TelegramBotService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UpdatesControllerTest {

    @Mock
    private TelegramBotService telegramBotService;

    @InjectMocks
    private UpdatesController updatesController;

    @BeforeEach
    void setUp() {
        lenient().when(telegramBotService.sendMessage(anyLong(), anyString())).thenReturn(true);
    }

    @Test
    void testSendUpdatesWithValidNewPullRequest() {
        LinkUpdate update = new LinkUpdate(
                1L,
                "https://github.com/owner/repo/pull/123",
                "New pull request",
                List.of(123456L),
                "pull_request",
                "#123 Add new feature",
                "john_doe",
                OffsetDateTime.now(),
                "This PR adds a new feature to the application...");

        ResponseEntity<?> response = updatesController.sendUpdates(update);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ResponseBodyLiterals.UPDATE_HANDLED, response.getBody());
        verify(telegramBotService, times(1)).sendMessage(eq(123456L), anyString());
    }

    @Test
    void testSendUpdatesWithValidNewIssue() {
        LinkUpdate update = new LinkUpdate(
                2L,
                "https://github.com/owner/repo/issues/456",
                "New issue",
                List.of(654321L),
                "issue",
                "#456 Bug report",
                "jane_smith",
                OffsetDateTime.now(),
                "Found a critical bug in the login module...");

        ResponseEntity<?> response = updatesController.sendUpdates(update);

        assertEquals(200, response.getStatusCode().value());
        verify(telegramBotService, times(1)).sendMessage(eq(654321L), anyString());
    }

    @Test
    void testSendUpdatesWithInvalidUpdateMissingUrl() {
        LinkUpdate update = new LinkUpdate(1L, null, "Invalid update", List.of(123456L));

        ResponseEntity<?> response = updatesController.sendUpdates(update);

        assertEquals(400, response.getStatusCode().value());
        verify(telegramBotService, never()).sendMessage(anyLong(), anyString());
    }

    @Test
    void testSendUpdatesWithEmptyTgChatIds() {
        LinkUpdate update = new LinkUpdate(1L, "https://github.com/owner/repo/pull/123", "Invalid update", List.of());

        ResponseEntity<?> response = updatesController.sendUpdates(update);

        assertEquals(400, response.getStatusCode().value());
        verify(telegramBotService, never()).sendMessage(anyLong(), anyString());
    }

    @Test
    void testSendUpdatesMultipleChatIds() {
        LinkUpdate update = new LinkUpdate(
                1L,
                "https://github.com/owner/repo/pull/123",
                "New PR",
                List.of(111L, 222L, 333L),
                "pull_request",
                "#123 Fix",
                "user",
                OffsetDateTime.now(),
                "Preview text");

        ResponseEntity<?> response = updatesController.sendUpdates(update);

        assertEquals(200, response.getStatusCode().value());
        verify(telegramBotService, times(3)).sendMessage(anyLong(), anyString());
    }

    @Test
    void testSendUpdatesWithoutChangeDetails() {
        LinkUpdate update = new LinkUpdate(1L, "https://github.com/owner/repo/pull/123", "Update", List.of(123456L));

        ResponseEntity<?> response = updatesController.sendUpdates(update);

        assertEquals(200, response.getStatusCode().value());
        verify(telegramBotService, times(1)).sendMessage(eq(123456L), anyString());
    }
}
