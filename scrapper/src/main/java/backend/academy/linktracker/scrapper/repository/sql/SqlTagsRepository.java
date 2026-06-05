package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.dto.entity.TagDto;
import backend.academy.linktracker.scrapper.repository.TagsRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "SQL")
@AllArgsConstructor
public class SqlTagsRepository implements TagsRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean create(Long linkId, String name) {
        String sql = "INSERT INTO link_tags (link_id, tag) VALUES (?, ?)";
        return jdbcTemplate.update(sql, linkId, name) > 0;
    }

    @Override
    public List<TagDto> read(Long linkId) {
        String sql = "SELECT tag FROM link_tags WHERE link_id = ?";
        return jdbcTemplate.query(sql, (rs, _) -> new TagDto(rs.getString("tag")), linkId);
    }

    @Override
    public boolean update(Long linkId, String newName) {
        String sql = "UPDATE link_tags SET tag = ? WHERE link_id = ?";
        return jdbcTemplate.update(sql, newName, linkId) > 0;
    }

    @Override
    public boolean delete(Long linkId, String name) {
        String sql = "DELETE FROM link_tags WHERE link_id = ? AND tag = ?";
        return jdbcTemplate.update(sql, linkId, name) > 0;
    }

    @Override
    public boolean exists(TagDto tag) {
        return false;
    }
}
