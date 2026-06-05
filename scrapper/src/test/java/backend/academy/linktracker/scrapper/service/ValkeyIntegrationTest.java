package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.IntegrationEnvironment;
import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import backend.academy.linktracker.scrapper.dto.entity.LinkDto;
import backend.academy.linktracker.scrapper.dto.result.AddLinkResult;
import backend.academy.linktracker.scrapper.dto.result.DeleteLinkResult;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.time.OffsetDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ValkeyIntegrationTest extends IntegrationEnvironment {

    @Autowired
    private LinksService linksService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ChatRepository chatRepository;

    private static final long TEST_CHAT_ID = 12345L;

    @BeforeEach
    void setUp() {
        var cache = cacheManager.getCache("userLinks");
        if (cache != null) {
            cache.clear();
        }

        ChatEntity chat = new ChatEntity();
        chat.setChatId(TEST_CHAT_ID);
        chatRepository.save(chat);
    }

    @Test
    void testCacheIsEmptyInitially() {
        var cache = cacheManager.getCache("userLinks");
        assertNotNull(cache);
    }

    @Test
    void testGetLinksForChatReturnsCachedValue() {
        Set<LinkDto> result1 = linksService.getLinksForChat(TEST_CHAT_ID);
        Set<LinkDto> result2 = linksService.getLinksForChat(TEST_CHAT_ID);

        assertEquals(result1, result2);
    }

    @Test
    void testCacheInvalidationOnAdd() {
        var cache = cacheManager.getCache("userLinks");
        assertNotNull(cache);

        LinkDto link = new LinkDto(-1, "https://github.com/test", null, OffsetDateTime.now());
        AddLinkResult result = linksService.addLinkToChat(TEST_CHAT_ID, link);

        assertEquals(AddLinkResult.OK, result);

        var cachedValue = cache.get(TEST_CHAT_ID);
        assertTrue(cachedValue == null || cachedValue.get() == null);
    }

    @Test
    void testCacheInvalidationOnDelete() {
        var cache = cacheManager.getCache("userLinks");
        assertNotNull(cache);

        LinkDto link = new LinkDto(-1, "https://github.com/test", null, OffsetDateTime.now());
        DeleteLinkResult result = linksService.removeLinkFromChat(TEST_CHAT_ID, link);

        assertEquals(DeleteLinkResult.CHAT_NOT_FOUND_OR_LINK_NOT_FOUND, result);

        var cachedValue = cache.get(TEST_CHAT_ID);
        assertTrue(cachedValue == null || cachedValue.get() == null);
    }
}
