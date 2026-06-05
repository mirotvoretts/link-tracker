package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import backend.academy.linktracker.scrapper.dto.entity.LinkDto;
import backend.academy.linktracker.scrapper.dto.entity.TagDto;
import backend.academy.linktracker.scrapper.dto.result.AddLinkResult;
import backend.academy.linktracker.scrapper.dto.result.DeleteLinkResult;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinksRepository;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinksService {

    private final LinksRepository linksRepository;
    private final ChatRepository chatRepository;
    private final TagService tagService;

    private LinkDto entityToDto(LinkEntity entity) {
        List<TagDto> tags =
                entity.getTags().stream().map(tag -> new TagDto(tag.getTag())).toList();
        return new LinkDto(entity.getId(), entity.getUrl(), tags, entity.getLastCheckedAt());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "userLinks", key = "#chatId")
    public Set<LinkDto> getLinksForChat(long chatId) {
        if (!linksRepository.chatExists(chatId)) {
            return Set.of();
        }

        return linksRepository.findByChatId(chatId).stream()
                .map(this::entityToDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    @CacheEvict(cacheNames = "userLinks", key = "#chatId")
    public AddLinkResult addLinkToChat(long chatId, LinkDto link) {
        if (!linksRepository.chatExists(chatId)) {
            return AddLinkResult.CHAT_NOT_FOUND;
        }

        if (linksRepository.existsByChatIdAndUrl(chatId, link.getUrl())) {
            return AddLinkResult.LINK_ALREADY_EXISTS;
        }

        try {
            ChatEntity chat = chatRepository.findById(chatId).orElseThrow();

            LinkEntity linkEntity = new LinkEntity();
            linkEntity.setChat(chat);
            linkEntity.setUrl(link.getUrl());
            linkEntity.setLastCheckedAt(OffsetDateTime.now());

            LinkEntity savedLink = linksRepository.save(linkEntity);
            link.setId(savedLink.getId());

            if (link.getTags() != null) {
                for (TagDto tag : link.getTags()) {
                    tagService.addTag(savedLink.getId(), tag.getName());
                }
            }

            return AddLinkResult.OK;
        } catch (java.util.NoSuchElementException _) {
            return AddLinkResult.CHAT_NOT_FOUND;
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "userLinks", key = "#chatId")
    public DeleteLinkResult removeLinkFromChat(long chatId, LinkDto link) {
        try {
            LinkEntity linkEntity =
                    linksRepository.findByChatIdAndUrl(chatId, link.getUrl()).orElseThrow();
            linksRepository.delete(linkEntity);
            return DeleteLinkResult.OK;
        } catch (java.util.NoSuchElementException _) {
            return DeleteLinkResult.CHAT_NOT_FOUND_OR_LINK_NOT_FOUND;
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, Set<LinkDto>> getAllLinksGroupedByChat() {
        List<LinkEntity> allLinks = linksRepository.findAll();
        Map<Long, Set<LinkDto>> result = new HashMap<>();

        for (LinkEntity link : allLinks) {
            long chatId = link.getChat().getChatId();
            result.computeIfAbsent(chatId, _ -> new HashSet<>()).add(entityToDto(link));
        }

        return result;
    }

    @Transactional
    public void updateLink(LinkDto link) {
        linksRepository.updateLastCheckedAt(link.getId(), link.getLastCheckedAt());
    }
}
