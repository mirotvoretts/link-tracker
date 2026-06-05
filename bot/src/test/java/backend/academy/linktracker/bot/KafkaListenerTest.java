package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.dto.LinkUpdateRequest;
import backend.academy.linktracker.bot.listener.LinkUpdatesKafkaListener;
import backend.academy.linktracker.bot.service.TelegramBotService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaListenerTest {

    @Mock
    private TelegramBotService telegramBotService;

    private LinkUpdatesKafkaListener kafkaListener;

    @BeforeEach
    void setUp() {
        kafkaListener = new LinkUpdatesKafkaListener(telegramBotService);
    }

    @Test
    void testKafkaListenerProcessesValidMessage() {
        OffsetDateTime now = OffsetDateTime.now();
        LinkUpdateRequest.LinkChangeDetails changeDetails =
                new LinkUpdateRequest.LinkChangeDetails("NEW_ISSUE", "Test Issue", "testuser", now, "Test preview");

        LinkUpdateRequest updateRequest = new LinkUpdateRequest(
                1L, "https://github.com/example/repo", "Test link", List.of(123456789L), changeDetails);

        kafkaListener.listen(updateRequest, null);

        String expectedMessage = String.format(
                "<b>Новое %s</b>%n%n"
                        + "<b>Название:</b> %s%n"
                        + "<b>Автор:</b> %s%n"
                        + "<b>Дата:</b> %s%n%n"
                        + "<b>Превью:</b>%n"
                        + "%s%n%n"
                        + "<a href=\"%s\">Перейти к обсуждению</a>",
                "new issue",
                "Test Issue",
                "testuser",
                now.toLocalDateTime().toString(),
                "Test preview",
                "https://github.com/example/repo");
        verify(telegramBotService).sendMessage(123456789L, expectedMessage);
    }

    @Test
    void testKafkaListenerRejectsInvalidUpdate() {
        LinkUpdateRequest invalidUpdate = new LinkUpdateRequest(0L, null, "description", List.of(), null);
        assertThrows(IllegalArgumentException.class, () -> kafkaListener.listen(invalidUpdate, null));
        verify(telegramBotService, never())
                .sendMessage(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testKafkaListenerWithoutChangeDetails() {
        LinkUpdateRequest updateRequest =
                new LinkUpdateRequest(1L, "https://github.com/example/repo", "Test link", List.of(123456789L), null);

        kafkaListener.listen(updateRequest, null);

        String expectedMessage = String.format("Обновление ссылки:%n%s", "https://github.com/example/repo");
        verify(telegramBotService).sendMessage(123456789L, expectedMessage);
    }
}
