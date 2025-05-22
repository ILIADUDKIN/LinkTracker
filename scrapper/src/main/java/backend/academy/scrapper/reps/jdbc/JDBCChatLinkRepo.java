package backend.academy.scrapper.reps.jdbc;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.reps.ChatLinkRepo;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessTypeConfig.class)
public class JDBCChatLinkRepo implements ChatLinkRepo {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isLinkAdded(Chat chat, Link link) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                """
                    SELECT count(*) FROM chats_links WHERE chat_id = ? AND link_id = ?
                    """,
                Integer.class,
                chat.id(),
                link.id())

            ).map(it -> it > 0)
            .orElse(false);
    }

    @Override
    public boolean addLink(Chat chat, Link link) {
        return jdbcTemplate.update("""
            INSERT INTO chats_links(chat_id, link_id) VALUES (?, ?)
            """, chat.id(), link.id()
        ) > 0;
    }

    @Override
    public boolean removeLink(Chat chat, Link link) {
        return jdbcTemplate.update(
            """
                DELETE FROM chats_links WHERE chat_id = ? AND link_id = ?
                """,
            chat.id(), link.id()
        ) > 0;
    }
}
