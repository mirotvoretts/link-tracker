package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.HelpCommand;
import backend.academy.linktracker.bot.command.ListCommand;
import backend.academy.linktracker.bot.command.StartCommand;
import backend.academy.linktracker.bot.command.TrackCommand;
import backend.academy.linktracker.bot.command.UntrackCommand;
import backend.academy.linktracker.bot.configuration.TelegramConfiguration;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommandsTest {

    @Test
    void startAndHelpMetadata() {
        StartCommand start = new StartCommand(null, null);
        HelpCommand help = new HelpCommand(null, List.of(start));

        assertEquals("/start", start.getUsage());
        assertEquals("Запустить бота", start.getDescription());

        assertEquals("/help", help.getUsage());
        assertEquals("Доступные команды", help.getDescription());
    }

    @Test
    void trackCommandMetadata() {
        TrackCommand track = new TrackCommand(null, null);
        assertEquals("/track", track.getUsage());
        assertEquals("Начать отслеживать ссылку", track.getDescription());
    }

    @Test
    void untrackCommandMetadata() {
        UntrackCommand untrack = new UntrackCommand(null, null);
        assertEquals("/untrack", untrack.getUsage());
        assertEquals("Перестать отслеживать ссылку", untrack.getDescription());
    }

    @Test
    void listCommandMetadata() {
        ListCommand list = new ListCommand(null, null);
        assertEquals("/list", list.getUsage());
        assertEquals("Вывести список всех отслеживаемых ссылок", list.getDescription());
    }

    @Test
    void telegramConfigurationCommandsMap() {
        TelegramConfiguration configuration = new TelegramConfiguration();

        Command command1 = new Command() {
            @Override
            public void execute(Update update) {}

            @Override
            public String getUsage() {
                return "/a";
            }

            @Override
            public String getDescription() {
                return "A command";
            }
        };

        Command command2 = new Command() {
            @Override
            public void execute(Update update) {}

            @Override
            public String getUsage() {
                return "/b";
            }

            @Override
            public String getDescription() {
                return "B command";
            }
        };

        Map<String, Command> map = configuration.commands(List.of(command1, command2));

        assertNotNull(map);
        assertEquals(2, map.size());
        assertSame(command1, map.get(command1.getUsage()));
        assertSame(command2, map.get(command2.getUsage()));
    }
}
