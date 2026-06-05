package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.ScrapperClient;
import backend.academy.linktracker.bot.dto.result.DeleteLinkResult;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class UntrackCommand implements Command {
    private final TelegramBot bot;
    private final ScrapperClient scrapperClient;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            bot.execute(new SendMessage(chatId, "Использование: /untrack <ссылка>"));
            return;
        }

        String link = parts[1].trim();
        try {
            DeleteLinkResult result = scrapperClient.removeLink(chatId, link);
            if (result.equals(DeleteLinkResult.OK)) {
                bot.execute(new SendMessage(chatId, "Ссылка успешно убрана: " + link));
            } else {
                bot.execute(new SendMessage(chatId, "Ссылка не найдена или чат не зарегистрирован."));
            }
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("link", link)
                    .addKeyValue("error", e.getMessage())
                    .log("Error removing link");
            bot.execute(new SendMessage(chatId, "Произошла ошибка при удалении ссылки."));
        }
    }

    @Override
    public String getUsage() {
        return "/untrack";
    }

    @Override
    public String getDescription() {
        return "Перестать отслеживать ссылку";
    }
}
