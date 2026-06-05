package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.dto.entity.TagDto;
import java.util.List;

public interface TagsRepository {
    boolean create(Long linkId, String name);

    List<TagDto> read(Long linkId);

    boolean update(Long linkId, String newName);

    boolean delete(Long linkId, String name);

    boolean exists(TagDto tag);
}
