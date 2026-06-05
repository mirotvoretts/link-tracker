package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinksEntityRepository extends JpaRepository<LinkEntity, Long> {
    @Query("SELECT l FROM LinkEntity l WHERE l.chat.chatId = :chatId AND l.url = :url")
    Optional<LinkEntity> findByChatIdAndUrl(@Param("chatId") Long chatId, @Param("url") String url);

    @Query("SELECT COUNT(l) > 0 FROM LinkEntity l WHERE l.chat.chatId = :chatId AND l.url = :url")
    boolean existsByChatIdAndUrl(@Param("chatId") Long chatId, @Param("url") String url);

    @Query("SELECT l FROM LinkEntity l WHERE l.chat.chatId = :chatId")
    List<LinkEntity> findByChatId(@Param("chatId") Long chatId);

    @Query("DELETE FROM LinkEntity l WHERE l.chat.chatId = :chatId")
    void deleteAllByChatId(@Param("chatId") Long chatId);
}
