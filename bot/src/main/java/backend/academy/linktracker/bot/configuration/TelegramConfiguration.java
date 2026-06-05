package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.property.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfiguration {

    @Bean
    public TelegramBot telegramBot(TelegramProperties properties) {
        var builder = new TelegramBot.Builder(properties.getToken())
                .apiUrl(properties.getUrl())
                .updateListenerSleep(properties.getUpdateListenerSleep().toMillis());

        if (properties.isDebug()) {
            builder.debug();
        }

        return builder.build();
    }

    @Bean
    public Map<String, Command> commands(List<Command> commands) {
        return commands.stream().collect(Collectors.toMap(Command::getUsage, command -> command));
    }

    @Bean
    public SetMyCommands setMyCommands(List<Command> commands) {
        var botCommands = commands.stream()
                .map(command -> new BotCommand(command.getUsage(), command.getDescription()))
                .toArray(BotCommand[]::new);
        return new SetMyCommands(botCommands);
    }
}
