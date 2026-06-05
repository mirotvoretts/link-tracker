package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import backend.academy.linktracker.scrapper.dto.entity.LinkDto;
import backend.academy.linktracker.scrapper.dto.result.AddLinkResult;
import backend.academy.linktracker.scrapper.dto.result.DeleteLinkResult;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinksRepository;
import backend.academy.linktracker.scrapper.service.LinksService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class LinksServiceTest {

    @Mock
    private LinksRepository linksRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private LinksService linksService;

    private final long chatId = 777L;

    @BeforeEach
    void setUp() {}

    @Test
    void addLinkSuccessfully() {
        LinkDto link = new LinkDto(0, "https://github.com/x/y", null, OffsetDateTime.now());
        LinkEntity savedLink = new LinkEntity();
        savedLink.setId(1L);
        savedLink.setUrl(link.getUrl());

        when(linksRepository.chatExists(chatId)).thenReturn(true);
        when(linksRepository.existsByChatIdAndUrl(chatId, link.getUrl())).thenReturn(false);

        ChatEntity chat = new ChatEntity();
        chat.setChatId(chatId);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(linksRepository.save(any(LinkEntity.class))).thenReturn(savedLink);

        AddLinkResult result = linksService.addLinkToChat(chatId, link);

        assertEquals(AddLinkResult.OK, result);
        verify(linksRepository).save(any(LinkEntity.class));
    }

    @Test
    void addLinkWhenChatNotFoundByRepository() {
        LinkDto link = new LinkDto(0, "https://github.com/x/y", null, OffsetDateTime.now());

        when(linksRepository.chatExists(chatId)).thenReturn(true);
        when(linksRepository.existsByChatIdAndUrl(chatId, link.getUrl())).thenReturn(false);
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        AddLinkResult result = linksService.addLinkToChat(chatId, link);

        assertEquals(AddLinkResult.CHAT_NOT_FOUND, result);
        verify(linksRepository, never()).save(any());
    }

    @Test
    void removeLinkSuccessfully() {
        LinkDto link = new LinkDto(0, "https://github.com/x/y", null, OffsetDateTime.now());

        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setId(1L);
        linkEntity.setUrl(link.getUrl());

        when(linksRepository.findByChatIdAndUrl(chatId, link.getUrl())).thenReturn(Optional.of(linkEntity));

        DeleteLinkResult result = linksService.removeLinkFromChat(chatId, link);

        assertEquals(DeleteLinkResult.OK, result);
        verify(linksRepository).delete(linkEntity);
    }

    @Test
    void removeLinkWhenNotFound() {
        LinkDto link = new LinkDto(0, "https://github.com/x/y", null, OffsetDateTime.now());

        when(linksRepository.findByChatIdAndUrl(chatId, link.getUrl())).thenReturn(Optional.empty());

        DeleteLinkResult result = linksService.removeLinkFromChat(chatId, link);

        assertEquals(DeleteLinkResult.CHAT_NOT_FOUND_OR_LINK_NOT_FOUND, result);
        verify(linksRepository, never()).delete(any());
    }

    @Test
    void getLinksForChat() {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setId(1L);
        linkEntity.setUrl("https://github.com/x/y");
        linkEntity.setLastCheckedAt(OffsetDateTime.now());

        when(linksRepository.chatExists(chatId)).thenReturn(true);
        when(linksRepository.findByChatId(chatId)).thenReturn(List.of(linkEntity));

        var result = linksService.getLinksForChat(chatId);

        assertEquals(1, result.size());
        verify(linksRepository).findByChatId(chatId);
    }
}
