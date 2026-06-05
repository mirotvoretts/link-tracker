package backend.academy.linktracker.scrapper.dto;

import backend.academy.linktracker.scrapper.dto.entity.LinkDto;

public record LinkWithChatId(long chatId, LinkDto link) {}
