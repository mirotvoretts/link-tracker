package backend.academy.linktracker.ai.dto;

import java.util.List;

public record RawUpdate(long id, String description, String author, List<Long> tgChatIds) {}
