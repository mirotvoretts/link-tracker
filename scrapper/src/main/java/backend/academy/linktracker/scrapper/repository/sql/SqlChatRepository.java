package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "SQL")
@AllArgsConstructor
public class SqlChatRepository implements ChatRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public ChatEntity save(ChatEntity chat) {
        String sql = "INSERT INTO chats (chat_id) VALUES (?)";
        try {
            jdbcTemplate.update(sql, chat.getChatId());
            return chat;
        } catch (DuplicateKeyException _) {
            return chat;
        }
    }

    @Override
    public void deleteById(long chatId) {
        String sql = "DELETE FROM chats WHERE chat_id = ?";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public Optional<ChatEntity> findById(long chatId) {
        if (!existsChat(chatId)) {
            return Optional.empty();
        }
        ChatEntity chat = new ChatEntity();
        chat.setChatId(chatId);
        return Optional.of(chat);
    }

    @Override
    public boolean existsChat(long chatId) {
        String sql = "SELECT COUNT(*) FROM chats WHERE chat_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null && count > 0;
    }

    @Override
    public boolean registerChat(long chatId) {
        if (existsChat(chatId)) {
            return false;
        }
        String sql = "INSERT INTO chats (chat_id) VALUES (?)";
        try {
            return jdbcTemplate.update(sql, chatId) > 0;
        } catch (DuplicateKeyException _) {
            return false;
        }
    }

    @Override
    public boolean deleteChat(long chatId) {
        if (!existsChat(chatId)) {
            return false;
        }
        String sql = "DELETE FROM chats WHERE chat_id = ?";
        return jdbcTemplate.update(sql, chatId) > 0;
    }
}
