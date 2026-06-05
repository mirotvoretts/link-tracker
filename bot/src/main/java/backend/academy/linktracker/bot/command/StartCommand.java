package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.ScrapperClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StartCommand implements Command {
    private final TelegramBot bot;
    private final ScrapperClient scrapperClient;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        try {
            scrapperClient.registerChat(chatId);
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("error", e.getMessage())
                    .log("Failed to register chat in scrapper (may already exist)");
        }
        bot.execute(
                new SendMessage(chatId, "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды."));
    }

    @Override
    public String getUsage() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Запустить бота";
    }
}
