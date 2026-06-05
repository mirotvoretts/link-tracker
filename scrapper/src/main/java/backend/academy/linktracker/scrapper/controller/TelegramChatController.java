package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.response.ResponseBodyLiterals;
import backend.academy.linktracker.scrapper.service.TelegramChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/tg-chat/{id}")
@RestController
@RequiredArgsConstructor
public class TelegramChatController {
    private final TelegramChatService telegramChatService;

    @PostMapping
    public ResponseEntity<?> registerChat(@PathVariable("id") Long chatId) {
        if (chatId == null || chatId <= 0) {
            return ResponseEntity.badRequest().body(ResponseBodyLiterals.INVALID_REQUEST_PARAMETERS);
        }

        boolean result = telegramChatService.registerChat(chatId);

        if (result) {
            return ResponseEntity.ok(ResponseBodyLiterals.CHAT_REGISTERED);
        } else {
            return ResponseEntity.status(409).body(ResponseBodyLiterals.CHAT_ALREADY_REGISTERED);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteChat(@PathVariable("id") Long chatId) {
        if (chatId == null || chatId <= 0) {
            return ResponseEntity.badRequest().body(ResponseBodyLiterals.INVALID_REQUEST_PARAMETERS);
        }

        boolean result = telegramChatService.deleteChat(chatId);

        if (result) {
            return ResponseEntity.ok(ResponseBodyLiterals.CHAT_DELETED);
        } else {
            return ResponseEntity.status(404).body(ResponseBodyLiterals.CHAT_NOT_FOUND);
        }
    }
}
