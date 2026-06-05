package backend.academy.linktracker.ai.dto;

import java.util.List;

public record ProcessedUpdate(long id, String description, List<Long> tgChatIds, String priority) {}
