package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.domain.jpa.ChatEntity;
import backend.academy.linktracker.scrapper.domain.jpa.LinkEntity;
import backend.academy.linktracker.scrapper.repository.LinksRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "SQL")
@AllArgsConstructor
public class SqlLinksRepository implements LinksRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean chatExists(long chatId) {
        String sql = "SELECT COUNT(*) FROM chats WHERE chat_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null && count > 0;
    }

    @Override
    public void removeChat(long chatId) {
        String sql = "DELETE FROM links WHERE chat_id = ?";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public List<LinkEntity> findByChatId(long chatId) {
        String sql = "SELECT id, chat_id, url, last_checked_at FROM links WHERE chat_id = ? ORDER BY id";
        return jdbcTemplate.query(sql, new LinkEntityRowMapper(), chatId);
    }

    @Override
    public boolean existsByChatIdAndUrl(long chatId, String url) {
        String sql = "SELECT COUNT(*) FROM links WHERE chat_id = ? AND url = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId, url);
        return count != null && count > 0;
    }

    @Override
    public Optional<LinkEntity> findByChatIdAndUrl(long chatId, String url) {
        String sql = "SELECT id, chat_id, url, last_checked_at FROM links WHERE chat_id = ? AND url = ?";
        try {
            LinkEntity link = jdbcTemplate.queryForObject(sql, new LinkEntityRowMapper(), chatId, url);
            return Optional.ofNullable(link);
        } catch (org.springframework.dao.EmptyResultDataAccessException _) {
            return Optional.empty();
        }
    }

    @Override
    public LinkEntity save(LinkEntity linkEntity) {
        String sql = "INSERT INTO links (chat_id, url, last_checked_at) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(
                    sql, linkEntity.getChat().getChatId(), linkEntity.getUrl(), linkEntity.getLastCheckedAt());

            String getIdSql = "SELECT id FROM links WHERE chat_id = ? AND url = ? ORDER BY id DESC LIMIT 1";
            Long linkId = jdbcTemplate.queryForObject(
                    getIdSql, Long.class, linkEntity.getChat().getChatId(), linkEntity.getUrl());

            if (linkId != null) {
                linkEntity.setId(linkId);
            }
            return linkEntity;
        } catch (org.springframework.dao.DataIntegrityViolationException _) {
            return linkEntity;
        }
    }

    @Override
    public void delete(LinkEntity linkEntity) {
        String sql = "DELETE FROM links WHERE id = ?";
        jdbcTemplate.update(sql, linkEntity.getId());
    }

    @Override
    public List<LinkEntity> findAll() {
        String sql = "SELECT id, chat_id, url, last_checked_at FROM links ORDER BY id";
        return jdbcTemplate.query(sql, new LinkEntityRowMapper());
    }

    @Override
    public void updateLastCheckedAt(long linkId, OffsetDateTime lastCheckedAt) {
        String sql = "UPDATE links SET last_checked_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, lastCheckedAt, linkId);
    }

    private static class LinkEntityRowMapper implements RowMapper<LinkEntity> {
        @Override
        public LinkEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            LinkEntity link = new LinkEntity();
            link.setId(rs.getLong("id"));
            link.setUrl(rs.getString("url"));

            Timestamp timestamp = rs.getTimestamp("last_checked_at");
            if (timestamp != null) {
                link.setLastCheckedAt(timestamp.toInstant().atOffset(java.time.ZoneOffset.UTC));
            }

            ChatEntity chat = new ChatEntity();
            chat.setChatId(rs.getLong("chat_id"));
            link.setChat(chat);

            return link;
        }
    }
}
