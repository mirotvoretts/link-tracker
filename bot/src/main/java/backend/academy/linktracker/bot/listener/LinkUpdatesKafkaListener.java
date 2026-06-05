package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.dto.LinkUpdateRequest;
import backend.academy.linktracker.bot.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdatesKafkaListener {

    private final TelegramBotService telegramBotService;

    @KafkaListener(topics = "link-updates", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(LinkUpdateRequest update, Acknowledgment acknowledgment) {
        try {
            putMdc(update);
            log.info("Received link update from Kafka");

            validateUpdate(update);

            var chatIds = update.tgChatIds();
            String message = formatUpdateMessage(update);

            for (var chatId : chatIds) {
                try {
                    MDC.put("chatId", String.valueOf(chatId));
                    telegramBotService.sendMessage(chatId, message);
                } catch (Exception e) {
                    log.error("Failed to send message to chat", e);
                } finally {
                    MDC.remove("chatId");
                }
            }
            log.info("Successfully processed link update from Kafka");

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (IllegalArgumentException e) {
            log.error("Validation error processing link update from Kafka. Message will be sent to DLQ", e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing link update from Kafka", e);
            throw e;
        } finally {
            clearMdc();
        }
    }

    private void validateUpdate(LinkUpdateRequest update) {
        if (update == null) {
            throw new IllegalArgumentException("Update is null");
        }
        if (update.id() <= 0) {
            throw new IllegalArgumentException("Invalid link ID: " + update.id());
        }
        if (update.url() == null || update.url().isEmpty()) {
            throw new IllegalArgumentException("URL is null or empty");
        }
        if (update.tgChatIds() == null || update.tgChatIds().isEmpty()) {
            throw new IllegalArgumentException("Chat IDs are null or empty");
        }
    }

    private void putMdc(LinkUpdateRequest update) {
        if (update != null) {
            MDC.put("linkId", String.valueOf(update.id()));
            MDC.put("url", update.url());
        }
    }

    private void clearMdc() {
        MDC.remove("linkId");
        MDC.remove("url");
    }

    private String formatUpdateMessage(LinkUpdateRequest update) {
        if (update.changeDetails() == null) {
            return String.format("Обновление ссылки:%n%s", update.url());
        }

        var details = update.changeDetails();
        return String.format(
                "<b>Новое %s</b>%n%n"
                        + "<b>Название:</b> %s%n"
                        + "<b>Автор:</b> %s%n"
                        + "<b>Дата:</b> %s%n%n"
                        + "<b>Превью:</b>%n"
                        + "%s%n%n"
                        + "<a href=\"%s\">Перейти к обсуждению</a>",
                details.changeType().replace("_", " ").toLowerCase(),
                escapeHtml(details.title()),
                escapeHtml(details.username()),
                details.createdAt() != null
                        ? details.createdAt().toLocalDateTime().toString()
                        : "NOT_AVAILABLE",
                escapeHtml(details.preview()),
                update.url());
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
