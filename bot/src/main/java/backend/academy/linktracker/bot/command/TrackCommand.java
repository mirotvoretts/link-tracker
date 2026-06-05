package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.state.ChatState;
import backend.academy.linktracker.bot.state.UserSession;
import backend.academy.linktracker.bot.state.UserSessionManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TrackCommand implements Command {
    private final TelegramBot bot;
    private final UserSessionManager sessionManager;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        UserSession session = sessionManager.getSession(chatId);
        session.setState(ChatState.AWAITING_LINK);
        session.setPendingLink(null);
        bot.execute(new SendMessage(chatId, "Отправьте ссылку для отслеживания:"));
    }

    @Override
    public String getUsage() {
        return "/track";
    }

    @Override
    public String getDescription() {
        return "Начать отслеживать ссылку";
    }
}
