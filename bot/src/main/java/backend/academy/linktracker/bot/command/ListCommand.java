package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.ScrapperClient;
import backend.academy.linktracker.bot.dto.entity.LinkDto;
import backend.academy.linktracker.bot.dto.request.ListLinksRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ListCommand implements Command {
    private final TelegramBot bot;
    private final ScrapperClient scrapperClient;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        String[] parts = text.split("\\s+", 2);
        String tagFilter = parts.length > 1 ? parts[1].trim() : null;

        try {
            ListLinksRequest response = scrapperClient.getLinks(chatId);
            if (response == null
                    || response.getLinks() == null
                    || response.getLinks().isEmpty()) {
                bot.execute(new SendMessage(chatId, "Нет отслеживаемых ссылок."));
                return;
            }

            List<LinkDto> links = response.getLinks();

            if (tagFilter != null && !tagFilter.isEmpty()) {
                links = links.stream()
                        .filter(link -> link.getTags() != null && link.getTags().contains(tagFilter))
                        .toList();

                if (links.isEmpty()) {
                    bot.execute(new SendMessage(chatId, "Нет отслеживаемых ссылок с тегом: " + tagFilter));
                    return;
                }
            }

            StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n\n");
            for (int i = 0; i < links.size(); i++) {
                LinkDto link = links.get(i);
                sb.append(i + 1).append(". ").append(link.getUrl());
                if (link.getTags() != null && !link.getTags().isEmpty()) {
                    sb.append(" [").append(String.join(", ", link.getTags())).append("]");
                }
                sb.append("\n");
            }

            bot.execute(new SendMessage(chatId, sb.toString()));
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("error", e.getMessage())
                    .log("Error getting links list");
            bot.execute(new SendMessage(chatId, "Нет отслеживаемых ссылок."));
        }
    }

    @Override
    public String getUsage() {
        return "/list";
    }

    @Override
    public String getDescription() {
        return "Вывести список всех отслеживаемых ссылок";
    }
}
