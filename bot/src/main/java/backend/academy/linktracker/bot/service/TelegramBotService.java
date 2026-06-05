package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {
    private final TelegramBot bot;

    public boolean update(long chatId, String url) {
        log.atInfo().addKeyValue("chatId", chatId).addKeyValue("url", url).log("Sending update notification to user");

        SendMessage message = new SendMessage(chatId, "Обнаружено обновление: " + url);
        SendResponse response = bot.execute(message);
        return response.isOk();
    }

    public boolean sendMessage(long chatId, String message) {
        log.atInfo().addKeyValue("chatId", chatId).log("Sending formatted message to user");

        SendMessage sendMessage = new SendMessage(chatId, message).parseMode(ParseMode.HTML);
        SendResponse response = bot.execute(sendMessage);
        return response.isOk();
    }
}
