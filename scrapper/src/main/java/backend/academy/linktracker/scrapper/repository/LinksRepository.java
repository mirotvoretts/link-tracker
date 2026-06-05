package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinksRepository {
    void removeChat(long chatId);

    List<LinkEntity> findByChatId(long chatId);

    boolean existsByChatIdAndUrl(long chatId, String url);

    Optional<LinkEntity> findByChatIdAndUrl(long chatId, String url);

    LinkEntity save(LinkEntity linkEntity);

    void delete(LinkEntity linkEntity);

    List<LinkEntity> findAll();

    boolean chatExists(long chatId);

    void updateLastCheckedAt(long linkId, OffsetDateTime lastCheckedAt);
}
