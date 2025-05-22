package backend.academy.scrapper.reps.jdbc;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.reps.ChatRepo;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessTypeConfig.class)
@RequiredArgsConstructor
public class JDBCChatRepo implements ChatRepo {

    private static final RowMapper<Chat> CHAT_ROW_MAPPER = (rs, rowNum) ->
        Chat.builder()
            .id(rs.getLong("id"))
            .chatId(rs.getLong("chat_id"))
            .build();
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Chat save(Chat chat) {
        return jdbcTemplate.queryForObject("""
            INSERT INTO chats(chat_id) VALUES (?) RETURNING id, chat_id
            """, CHAT_ROW_MAPPER, chat.chatId());
    }

    @Override
    public boolean delete(Long chatId) {
        return jdbcTemplate.update("DELETE FROM chats WHERE chat_id = ?", chatId) > 0;
    }

    @Override
    public Optional<Chat> findByChatId(Long chatId) {
        return jdbcTemplate.queryForStream("""
            SELECT id, chat_id FROM chats WHERE chat_id = ?
            """, CHAT_ROW_MAPPER, chatId).findFirst();
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("""
                SELECT count(id) FROM chats WHERE chat_id = ?
                """, Integer.class, chatId))
            .map(it -> it > 0)
            .orElse(false);
    }

    @Override
    public Optional<Chat> findWithLinksByChatId(Long chatId) {
        return Optional.empty();
    }
}
