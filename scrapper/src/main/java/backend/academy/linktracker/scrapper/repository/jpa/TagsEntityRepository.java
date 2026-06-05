package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.jpa.TagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagsEntityRepository extends JpaRepository<TagEntity, Long> {
    List<TagEntity> findByLinkId(Long linkId);

    @Modifying
    @Query("UPDATE TagEntity t SET t.tag = :newName WHERE t.link.id = :linkId")
    void updateTagByLinkId(@Param("linkId") Long linkId, @Param("newName") String newName);

    @Modifying
    void deleteByLinkIdAndTag(Long linkId, String tag);

    boolean existsByTag(String tag);
}
