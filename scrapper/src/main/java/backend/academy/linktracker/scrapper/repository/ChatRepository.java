package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import java.util.Optional;

public interface ChatRepository {
    boolean existsChat(long chatId);

    ChatEntity save(ChatEntity chat);

    void deleteById(long chatId);

    Optional<ChatEntity> findById(long chatId);

    boolean registerChat(long chatId);

    boolean deleteChat(long chatId);
}
