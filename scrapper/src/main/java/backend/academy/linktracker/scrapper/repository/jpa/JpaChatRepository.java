package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class JpaChatRepository implements ChatRepository {

    private final ChatEntityRepository chatEntityRepository;

    @Override
    @Transactional
    public ChatEntity save(ChatEntity chat) {
        return chatEntityRepository.save(chat);
    }

    @Override
    @Transactional
    public void deleteById(long chatId) {
        chatEntityRepository.deleteById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatEntity> findById(long chatId) {
        return chatEntityRepository.findById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsChat(long chatId) {
        return chatEntityRepository.existsById(chatId);
    }

    @Override
    @Transactional
    public boolean registerChat(long chatId) {
        if (chatEntityRepository.existsById(chatId)) {
            return false;
        }
        ChatEntity chat = new ChatEntity();
        chat.setChatId(chatId);
        chatEntityRepository.save(chat);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteChat(long chatId) {
        if (!chatEntityRepository.existsById(chatId)) {
            return false;
        }
        chatEntityRepository.deleteById(chatId);
        return true;
    }
}
