package backend.academy.scrapper.reps.jdbc;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.linkConditions.LinkStatus;
import backend.academy.scrapper.reps.LinkRepo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessTypeConfig.class)
public class JDBCLinkRepo implements LinkRepo {

    private final JdbcTemplate jdbcTemplate;
    private final LinkMapper linkMapper;
    private final LinkWithChatsResultSetExtractor linkWithChatsResultSetExtractor;

    @Override
    public Link save(Link link) {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO links(link_type, url, checked_at, status)
                VALUES (?, ?, ?, ?)
                RETURNING id, link_type, url, checked_at, status
                """,
            linkMapper,
            link.linkType().ordinal(),
            link.url(),
            link.checkedAt(),
            link.status().ordinal()
        );
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return jdbcTemplate.query("""
                SELECT l.id, l.link_type, l.url, l.checked_at, l.status
                FROM links l
                JOIN chats_links cl ON l.id = cl.link_id
                WHERE cl.chat_id = ?
                """,
            linkMapper,
            chat.id()
        );
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return jdbcTemplate.queryForStream("""
                SELECT id, link_type, url, checked_at, status FROM links WHERE url = ?
                """,
            linkMapper,
            url).findFirst();
    }

    @Override
    public boolean updateStatus(Link link, LinkStatus status) {
        return jdbcTemplate.update("""
                UPDATE links SET status = ? WHERE id = ?
                """,
            status.ordinal(), link.id()) > 0;
    }

    @Override
    public boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        return jdbcTemplate.update("""
                UPDATE links SET checked_at = ? WHERE id = ?
                """,
            checkedAt, link.id()) > 0;
    }

    @Override
    public List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Integer limit) {

        return jdbcTemplate.query("""
            SELECT l.id, l.link_type, l.url, l.checked_at, l.status,
                c.id AS c_id, c.chat_id AS chat_id
            FROM links l
            JOIN chats_links cl ON l.id = cl.link_id
            JOIN chats c ON cl.chat_id = c.id
            WHERE l.status = ? AND l.checked_at < ?
            LIMIT ?
            """, linkWithChatsResultSetExtractor, status.ordinal(), checkedAt, limit
        );
    }
}
