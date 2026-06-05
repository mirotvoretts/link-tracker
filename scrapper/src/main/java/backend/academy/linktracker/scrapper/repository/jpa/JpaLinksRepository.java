package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import backend.academy.linktracker.scrapper.repository.LinksRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class JpaLinksRepository implements LinksRepository {

    private final ChatEntityRepository chatEntityRepository;
    private final LinksEntityRepository linksEntityRepository;

    @Override
    @Transactional
    public void removeChat(long chatId) {
        linksEntityRepository.deleteAllByChatId(chatId);
        chatEntityRepository.deleteById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkEntity> findByChatId(long chatId) {
        return linksEntityRepository.findByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByChatIdAndUrl(long chatId, String url) {
        return linksEntityRepository.existsByChatIdAndUrl(chatId, url);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LinkEntity> findByChatIdAndUrl(long chatId, String url) {
        return linksEntityRepository.findByChatIdAndUrl(chatId, url);
    }

    @Override
    @Transactional
    public LinkEntity save(LinkEntity linkEntity) {
        return linksEntityRepository.save(linkEntity);
    }

    @Override
    @Transactional
    public void delete(LinkEntity linkEntity) {
        linksEntityRepository.delete(linkEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkEntity> findAll() {
        return linksEntityRepository.findAll();
    }

    @Override
    @Transactional
    public boolean chatExists(long chatId) {
        return chatEntityRepository.existsById(chatId);
    }

    @Override
    @Transactional
    public void updateLastCheckedAt(long linkId, OffsetDateTime lastCheckedAt) {
        linksEntityRepository.findById(linkId).ifPresent(link -> {
            link.setLastCheckedAt(lastCheckedAt);
            linksEntityRepository.save(link);
        });
    }
}
