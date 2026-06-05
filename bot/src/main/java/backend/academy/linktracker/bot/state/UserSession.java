package backend.academy.linktracker.bot.state;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private ChatState state = ChatState.NONE;
    private String pendingLink;

    public void reset() {
        state = ChatState.NONE;
        pendingLink = null;
    }
}
