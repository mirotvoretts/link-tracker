package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.entity.LinkUpdate;
import backend.academy.linktracker.bot.dto.response.ApiErrorResponse;
import backend.academy.linktracker.bot.dto.response.ResponseBodyLiterals;
import backend.academy.linktracker.bot.service.TelegramBotService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class UpdatesController {
    private final TelegramBotService telegramBotService;

    private boolean isValidUpdate(LinkUpdate update) {
        return update != null
                && update.getId() > 0
                && update.getUrl() != null
                && !update.getUrl().isEmpty()
                && !update.getTgChatIds().isEmpty();
    }

    @PostMapping
    @RateLimiter(name = "updates")
    public ResponseEntity<?> sendUpdates(@RequestBody LinkUpdate update) {
        if (isValidUpdate(update)) {

            var chatIds = update.getTgChatIds();
            String message = formatUpdateMessage(update);

            for (var chatId : chatIds) {
                telegramBotService.sendMessage(chatId, message);
            }

            return ResponseEntity.ok(ResponseBodyLiterals.UPDATE_HANDLED);
        } else {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    ResponseBodyLiterals.INVALID_REQUEST_PARAMETERS, "INVALID_UPDATE", null, null, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @SuppressWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String formatUpdateMessage(LinkUpdate update) {
        if (update.getChangeType() == null || update.getTitle() == null) {
            return String.format("Обновление ссылки:%n%s", update.getUrl());
        }

        return String.format(
                "<b>Новое %s</b>%n%n"
                        + "<b>Название:</b> %s%n"
                        + "<b>Автор:</b> %s%n"
                        + "<b>Дата:</b> %s%n%n"
                        + "<b>Превью:</b>%n"
                        + "%s%n%n"
                        + "<a href=\"%s\">Перейти к обсуждению</a>",
                update.getChangeType().replace("_", " ").toLowerCase(),
                escapeHtml(update.getTitle()),
                escapeHtml(update.getUsername()),
                update.getCreatedAt() != null
                        ? update.getCreatedAt().toLocalDateTime().toString()
                        : "NOT_AVAILABLE",
                escapeHtml(update.getPreview()),
                update.getUrl());
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
