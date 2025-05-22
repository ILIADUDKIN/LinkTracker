package backend.academy.scrapper.repsTest;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.config.JdbcTestConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.integrationTest.IntegrationTest;
import backend.academy.scrapper.reps.jdbc.JDBCChatLinkRepo;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JDBCChatLinkRepo.class})
@ContextConfiguration(classes = {JdbcTestConfig.class, DatabaseAccessConfig.JdbcAccessTypeConfig.class})
@TestPropertySource(properties = {"app.database-access-type=jdbc"})
class ClTest extends IntegrationTest {

    private static final Chat CHAT = Chat.builder().id(1L).build();
    private static final Link LINK = Link.builder().id(1L).build();
    @Autowired
    private JDBCChatLinkRepo chatLinkRepository;

    @Nested
    class IsLinkAddedToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql", "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkIsAddedToChat() {
            boolean isLinkAddedToChat = chatLinkRepository.isLinkAdded(CHAT, LINK);

            assertTrue(isLinkAddedToChat);
        }

        @Test
        @Transactional
        void shouldReturnFalseWhenLinkIsNotAddedToChat() {
            boolean isLinkAddedToChat = chatLinkRepository.isLinkAdded(CHAT, LINK);

            assertFalse(isLinkAddedToChat);
        }
    }

    @Nested
    class AddLinkToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkWasAdded() {
            boolean isAdded = chatLinkRepository.addLink(CHAT, LINK);

            assertTrue(isAdded);
        }
    }

    @Nested
    class RemoveLinkFromChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql", "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkWasRemoved() {
            boolean isRemoved = chatLinkRepository.removeLink(CHAT, LINK);

            assertTrue(isRemoved);
        }
    }
}
