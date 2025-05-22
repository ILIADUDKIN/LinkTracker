package backend.academy.scrapper.repsTest;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.config.JdbcTestConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.integrationTest.IntegrationTest;
import backend.academy.scrapper.reps.jdbc.JDBCChatRepo;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JDBCChatRepo.class})
@ContextConfiguration(classes = {JdbcTestConfig.class, DatabaseAccessConfig.JdbcAccessTypeConfig.class})
@TestPropertySource(properties = {"app.database-access-type=jdbc"})
class CTest extends IntegrationTest {

    private static final Chat CHAT = Chat.builder().chatId(123L).build();
    @Autowired
    private JDBCChatRepo chatRepository;

    @Nested
    class SaveChatTest {

        @Test
        @Transactional
        @Rollback
        void shouldReturnSavedChat() {
            Chat saved = chatRepository.save(CHAT);

            assertThat(saved).isNotNull();
            assertThat(saved.id()).isNotNull();
            assertThat(saved.chatId()).isEqualTo(123);
        }
    }

    @Nested
    class DeleteChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenChatWasDeleted() {
            boolean isRemoved = chatRepository.delete(CHAT.chatId());

            assertTrue(isRemoved);
        }
    }

    @Nested
    class FindByChatIdTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnChat() {
            Optional<Chat> chat = chatRepository.findByChatId(CHAT.chatId());

            assertThat(chat).isPresent();
            assertThat(chat.get().id()).isEqualTo(1);
            assertThat(chat.get().chatId()).isEqualTo(123);
        }
    }

    @Nested
    class ExistsByChatIdTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenChatExists() {
            boolean exists = chatRepository.existsByChatId(CHAT.chatId());

            assertTrue(exists);
        }

        @Test
        @Transactional
        void shouldReturnFalseWhenChatDoesNotExists() {
            boolean exists = chatRepository.existsByChatId(CHAT.chatId());

            assertFalse(exists);
        }
    }
}
