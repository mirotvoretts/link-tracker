package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.database-access-type=SQL")
@org.springframework.transaction.annotation.Transactional
@Tag("integration")
class SqlChatRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private ChatRepository chatRepository;

    @Test
    void registerAndDeleteChat() {

        long chatId = 123L;
        assertFalse(chatRepository.existsChat(chatId));

        assertTrue(chatRepository.registerChat(chatId));
        assertTrue(chatRepository.existsChat(chatId));

        assertFalse(chatRepository.registerChat(chatId));

        assertTrue(chatRepository.deleteChat(chatId));
        assertFalse(chatRepository.existsChat(chatId));

        assertFalse(chatRepository.deleteChat(chatId));
    }
}
