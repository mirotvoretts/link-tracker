package backend.academy.linktracker.bot.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class UserSessionManager {
    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession getSession(long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new UserSession());
    }

    public void resetSession(long chatId) {
        UserSession session = sessions.get(chatId);
        if (session != null) {
            session.reset();
        }
    }
}
