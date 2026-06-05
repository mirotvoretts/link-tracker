package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.ScrapperClient;
import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.UnknownCommandHandler;
import backend.academy.linktracker.bot.dto.result.AddLinkResult;
import backend.academy.linktracker.bot.state.ChatState;
import backend.academy.linktracker.bot.state.UserSession;
import backend.academy.linktracker.bot.state.UserSessionManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TelegramUpdatesListener implements UpdatesListener {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");

    private final TelegramBot bot;
    private final UnknownCommandHandler unknownCommandHandler;
    private final Map<String, Command> commands;
    private final UserSessionManager sessionManager;
    private final ScrapperClient scrapperClient;

    public TelegramUpdatesListener(
            @Qualifier("telegramBot") TelegramBot bot,
            @Qualifier("commands") Map<String, Command> commands,
            @Qualifier("setMyCommands") SetMyCommands setMyCommands,
            UserSessionManager sessionManager,
            ScrapperClient scrapperClient) {
        this.bot = bot;
        this.unknownCommandHandler = new UnknownCommandHandler(bot);
        this.commands = commands;
        this.sessionManager = sessionManager;
        this.scrapperClient = scrapperClient;
        bot.setUpdatesListener(this);
        try {
            bot.execute(setMyCommands);
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("error", e.getMessage())
                    .log("Failed to set bot commands on startup (will retry on next restart)");
        }
    }

    @Override
    public int process(List<Update> list) {
        list.forEach(update -> {
            if (update.message() != null) {
                onUpdateReceived(update);
            }
        });
        return CONFIRMED_UPDATES_ALL;
    }

    private void onUpdateReceived(Update update) {
        long chatId = update.message().chat().id();
        String messageText = update.message().text();

        MDC.put("chatId", String.valueOf(chatId));
        MDC.put("messageText", messageText);

        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("text", messageText)
                .log("Received Telegram update");

        try {
            if (messageText == null) {
                unknownCommandHandler.handle(update);
                return;
            }

            if (isCommand(messageText)) {
                UserSession session = sessionManager.getSession(chatId);
                if (session.getState() != ChatState.NONE && !messageText.startsWith("/cancel")) {
                    sessionManager.resetSession(chatId);
                }

                String commandKey = messageText.split("\\s+")[0];
                Command command = commands.get(commandKey);
                if (command != null) {
                    command.execute(update);
                } else {
                    unknownCommandHandler.handle(update);
                }
            } else {
                handleStateMachineMessage(chatId, messageText, update);
            }
        } finally {
            MDC.clear();
        }
    }

    private void handleStateMachineMessage(long chatId, String text, Update update) {
        UserSession session = sessionManager.getSession(chatId);

        switch (session.getState()) {
            case AWAITING_LINK -> {
                if (!URL_PATTERN.matcher(text).matches()) {
                    bot.execute(
                            new SendMessage(
                                    chatId,
                                    "Некорректная ссылка. Ссылка должна начинаться с http:// или https://. Попробуйте ещё раз:"));
                    return;
                }
                session.setPendingLink(text.trim());
                session.setState(ChatState.AWAITING_TAGS);
                bot.execute(new SendMessage(chatId, "Введите теги через запятую (или отправьте \"-\" для пропуска):"));
            }
            case AWAITING_TAGS -> {
                String link = session.getPendingLink();
                List<String> tags = List.of();
                if (text != null
                        && !text.isBlank()
                        && !"-".equals(text.trim())
                        && !"нет".equalsIgnoreCase(text.trim())) {
                    tags = Arrays.stream(text.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                }

                try {
                    AddLinkResult result = scrapperClient.addLink(chatId, link, tags);
                    switch (result) {
                        case AddLinkResult.OK -> bot.execute(new SendMessage(chatId, "Ссылка добавлена: " + link));
                        case AddLinkResult.ALREADY_EXISTS ->
                            bot.execute(new SendMessage(chatId, "Ссылка уже отслеживается"));
                        case AddLinkResult.NOT_FOUND ->
                            bot.execute(new SendMessage(chatId, "Чат не зарегистрирован. Используйте /start"));
                        default -> bot.execute(new SendMessage(chatId, "Произошла ошибка при добавлении ссылки."));
                    }
                } catch (Exception e) {
                    log.atError()
                            .addKeyValue("chatId", chatId)
                            .addKeyValue("link", link)
                            .addKeyValue("error", e.getMessage())
                            .log("Error adding link via scrapper");
                    bot.execute(new SendMessage(chatId, "Произошла ошибка при добавлении ссылки."));
                }

                sessionManager.resetSession(chatId);
            }
            default -> unknownCommandHandler.handle(update);
        }
    }

    private boolean isCommand(String messageText) {
        return messageText.startsWith("/");
    }
}
