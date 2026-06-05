package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.entity.TagDto;
import backend.academy.linktracker.scrapper.repository.TagsRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TagService {
    private final TagsRepository tagsRepository;

    @Transactional
    public boolean addTag(Long linkId, String name) {
        return tagsRepository.create(linkId, name);
    }

    @Transactional
    public List<TagDto> getTags(Long linkId) {
        return tagsRepository.read(linkId);
    }

    @Transactional
    public boolean updateTag(Long linkId, String newName) {
        return tagsRepository.update(linkId, newName);
    }

    @Transactional
    public boolean deleteTag(Long linkId, String name) {
        return tagsRepository.delete(linkId, name);
    }
}
