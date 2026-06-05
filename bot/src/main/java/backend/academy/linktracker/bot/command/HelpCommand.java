package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class HelpCommand implements Command {
    private final TelegramBot bot;
    private final List<Command> commandsSequence;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        bot.execute(new SendMessage(chatId, getAvailableCommands()));
    }

    @Override
    public String getUsage() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Доступные команды";
    }

    private String getAvailableCommands() {
        StringBuilder stringBuilder = new StringBuilder("Доступные команды:\n");
        stringBuilder.append(getUsage()).append(" - ").append(getDescription()).append("\n");
        for (Command command : commandsSequence) {
            stringBuilder
                    .append(command.getUsage())
                    .append(" - ")
                    .append(command.getDescription())
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
