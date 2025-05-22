package backend.academy.scrapper.repsTest;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.config.JdbcTestConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.integrationTest.IntegrationTest;
import backend.academy.scrapper.linkConditions.LinkStatus;
import backend.academy.scrapper.linkConditions.LinkType;
import backend.academy.scrapper.reps.jdbc.JDBCLinkRepo;
import backend.academy.scrapper.reps.jdbc.LinkMapper;
import backend.academy.scrapper.reps.jdbc.LinkWithChatsResultSetExtractor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JDBCLinkRepo.class, LinkMapper.class, LinkWithChatsResultSetExtractor.class})
@ContextConfiguration(classes = {JdbcTestConfig.class, DatabaseAccessConfig.JdbcAccessTypeConfig.class})
@TestPropertySource(properties = {"app.database-access-type=jdbc"})
class LTest extends IntegrationTest {

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK =
        Link.builder()
            .linkType(LinkType.GITHUB)
            .url("https://github.com/JetBrains/kotlin")
            .checkedAt(CHECKED_AT)
            .build();
    private static final Chat CHAT = Chat.builder().id(1L).chatId(123L).build();
    @Autowired
    private JDBCLinkRepo linkRepository;

    @Nested
    class SaveLinkTest {

        @Test
        @Transactional
        @Rollback
        void shouldReturnSavedLink() {
            Link saved = linkRepository.save(LINK);

            assertThat(saved).isNotNull();
            assertThat(saved.id()).isNotNull();
            assertThat(saved.status()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class FindAllByChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql",
            "/sql/chats-links/add-links-list-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnLinks() {
            List<Link> links = linkRepository.findAllByChat(CHAT);

            assertThat(links.size()).isEqualTo(2);

            Link first = links.get(0);
            assertThat(first.id()).isEqualTo(1);
            assertThat(first.linkType()).isEqualTo(LinkType.GITHUB);
            assertThat(first.url()).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(first.checkedAt()).isEqualTo(CHECKED_AT);
            assertThat(first.status()).isEqualTo(LinkStatus.ACTIVE);

            Link second = links.get(1);
            assertThat(second.id()).isEqualTo(2);
            assertThat(second.linkType()).isEqualTo(LinkType.STACKOVERFLOW);
            assertThat(second.url()).isEqualTo("https://stackoverflow.com/questions/24840667");
            assertThat(second.checkedAt()).isEqualTo(CHECKED_AT);
            assertThat(second.status()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class FindByUrlTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnLink() {
            Optional<Link> link = linkRepository.findByUrl("https://github.com/JetBrains/kotlin");

            assertThat(link).isPresent();
            assertThat(link.get().id()).isEqualTo(1);
            assertThat(link.get().linkType()).isEqualTo(LinkType.GITHUB);
        }

        @Test
        @Transactional
        void shouldReturnNullWhenLinkDoesNotExists() {
            Optional<Link> link = linkRepository.findByUrl("https://github.com/JetBrains/kotlin");

            assertThat(link).isEmpty();
        }
    }

    @Nested
    class UpdateStatusTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenStatusWasUpdated() {
            boolean isUpdated = linkRepository.updateStatus(Link.builder().id(1L).build(), LinkStatus.BROKEN);

            assertTrue(isUpdated);

            Optional<Link> link = linkRepository.findByUrl(LINK.url());

            assertThat(link).isPresent();
            assertThat(link.get().id()).isEqualTo(1);
            assertThat(link.get().status()).isEqualTo(LinkStatus.BROKEN);
        }
    }

    @Nested
    class UpdateCheckedAtTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenCheckedAtWasUpdated() {
            boolean isUpdated = linkRepository.updateCheckedAt(Link.builder().id(1L).build(), CHECKED_AT.plusHours(1));

            assertTrue(isUpdated);

            Optional<Link> link = linkRepository.findByUrl(LINK.url());

            assertThat(link).isPresent();
            assertThat(link.get().id()).isEqualTo(1);
            assertThat(link.get().checkedAt()).isEqualTo(CHECKED_AT.plusHours(1));
        }
    }
}
