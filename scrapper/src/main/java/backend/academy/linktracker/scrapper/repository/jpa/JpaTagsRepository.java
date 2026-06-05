package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import backend.academy.linktracker.scrapper.domain.jpa.TagEntity;
import backend.academy.linktracker.scrapper.dto.entity.TagDto;
import backend.academy.linktracker.scrapper.repository.TagsRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class JpaTagsRepository implements TagsRepository {
    private final TagsEntityRepository tagsEntityRepository;

    @Override
    @Transactional
    public boolean create(Long linkId, String name) {
        try {
            TagEntity tagEntity = new TagEntity();
            LinkEntity link = new LinkEntity();
            link.setId(linkId);
            tagEntity.setLink(link);
            tagEntity.setTag(name);
            tagsEntityRepository.save(tagEntity);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    @Transactional
    public List<TagDto> read(Long linkId) {
        return tagsEntityRepository.findByLinkId(linkId).stream()
                .map(tag -> new TagDto(tag.getTag()))
                .toList();
    }

    @Override
    @Transactional
    public boolean update(Long linkId, String newName) {
        try {
            tagsEntityRepository.updateTagByLinkId(linkId, newName);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean delete(Long linkId, String name) {
        try {
            tagsEntityRepository.deleteByLinkIdAndTag(linkId, name);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean exists(TagDto tag) {
        return tagsEntityRepository.existsByTag(tag.getName());
    }
}
