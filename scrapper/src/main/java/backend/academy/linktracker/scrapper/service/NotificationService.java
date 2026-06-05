package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.request.LinkUpdateRequest;

public interface NotificationService {
    void sendUpdate(LinkUpdateRequest updateRequest);

    String getName();
}
