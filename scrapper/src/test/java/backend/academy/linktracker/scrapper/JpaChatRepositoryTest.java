package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.database-access-type=ORM")
@org.springframework.transaction.annotation.Transactional
@Tag("integration")
class JpaChatRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private ChatRepository chatRepository;

    @BeforeEach
    void setUp() {}

    @Test
    void registerAndDeleteChat() {
        long chatId = 123L;
        assertFalse(chatRepository.existsChat(chatId));

        assertTrue(chatRepository.registerChat(chatId), "First registration should succeed");
        assertTrue(chatRepository.existsChat(chatId), "Chat should exist after registration");

        assertFalse(chatRepository.registerChat(chatId), "Second registration attempt should fail");

        assertTrue(chatRepository.deleteChat(chatId), "Deletion should succeed");
        assertFalse(chatRepository.existsChat(chatId), "Chat should not exist after deletion");

        assertFalse(chatRepository.deleteChat(chatId), "Second deletion attempt should fail");
    }
}
