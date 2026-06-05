package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.state.UserSessionManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CancelCommand implements Command {
    private final UserSessionManager sessionManager;
    private final TelegramBot telegramBot;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        sessionManager.resetSession(chatId);
    }

    @Override
    public String getUsage() {
        return "/cancel";
    }

    @Override
    public String getDescription() {
        return "Прервать текущую операцию";
    }
}
