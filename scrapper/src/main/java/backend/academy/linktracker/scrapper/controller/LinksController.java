package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.entity.LinkDto;
import backend.academy.linktracker.scrapper.dto.entity.TagDto;
import backend.academy.linktracker.scrapper.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.response.ResponseBodyLiterals;
import backend.academy.linktracker.scrapper.dto.result.AddLinkResult;
import backend.academy.linktracker.scrapper.dto.result.DeleteLinkResult;
import backend.academy.linktracker.scrapper.service.LinksService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksController {
    private final LinksService linksService;

    private boolean isValidRequestParameters(Long chatId, String link) {
        return chatId != null && chatId > 0 && link != null && !link.isEmpty();
    }

    @PostMapping
    public ResponseEntity<?> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest addLinkRequest) {
        String link = addLinkRequest.link();

        if (isValidRequestParameters(chatId, link)) {
            List<TagDto> tags = addLinkRequest.tags() != null
                    ? addLinkRequest.tags().stream().map(TagDto::new).toList()
                    : List.of();
            LinkDto linkDto = new LinkDto(-1, link, tags, OffsetDateTime.now());

            AddLinkResult result = linksService.addLinkToChat(chatId, linkDto);

            switch (result) {
                case OK -> {
                    return ResponseEntity.ok(ResponseBodyLiterals.LINK_ADDED);
                }
                case CHAT_NOT_FOUND -> {
                    return ResponseEntity.status(404).body(ResponseBodyLiterals.CHAT_NOT_FOUND);
                }
                case LINK_ALREADY_EXISTS -> {
                    return ResponseEntity.status(409).body(ResponseBodyLiterals.LINK_ALREADY_EXISTS);
                }
                default -> {
                    return ResponseEntity.status(500).body(ResponseBodyLiterals.UNEXPECTED_ERROR);
                }
            }
        }

        return ResponseEntity.badRequest().body(ResponseBodyLiterals.INVALID_REQUEST_PARAMETERS);
    }

    @DeleteMapping
    public ResponseEntity<?> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        String link = removeLinkRequest.link();

        if (isValidRequestParameters(chatId, link)) {

            LinkDto linkDto = new LinkDto(-1, link, null, null);

            DeleteLinkResult result = linksService.removeLinkFromChat(chatId, linkDto);

            switch (result) {
                case OK -> {
                    return ResponseEntity.ok(ResponseBodyLiterals.LINK_DELETED);
                }
                case CHAT_NOT_FOUND_OR_LINK_NOT_FOUND -> {
                    return ResponseEntity.status(404).body(ResponseBodyLiterals.CHAT_NOT_FOUND_OR_LINK_NOT_FOUND);
                }
                default -> {
                    return ResponseEntity.status(500).body(ResponseBodyLiterals.UNEXPECTED_ERROR);
                }
            }
        }

        return ResponseEntity.badRequest().body(ResponseBodyLiterals.INVALID_REQUEST_PARAMETERS);
    }

    @GetMapping
    public ResponseEntity<?> getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        if (isValidRequestParameters(chatId, "link")) {
            Set<LinkDto> links = linksService.getLinksForChat(chatId);

            if (links.isEmpty()) {
                return ResponseEntity.status(404).body(ResponseBodyLiterals.CHAT_NOT_FOUND);
            }

            List<LinkResponse> responseLinks = links.stream()
                    .map(dto -> new LinkResponse(
                            dto.getId(),
                            dto.getUrl(),
                            dto.getTags() != null
                                    ? dto.getTags().stream()
                                            .map(TagDto::getName)
                                            .toList()
                                    : List.of()))
                    .toList();

            return ResponseEntity.ok(new ListLinksResponse(responseLinks, responseLinks.size()));
        }
        return ResponseEntity.badRequest().body(ResponseBodyLiterals.INVALID_REQUEST_PARAMETERS);
    }
}
