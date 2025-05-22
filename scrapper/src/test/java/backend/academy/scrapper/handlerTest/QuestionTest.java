package backend.academy.scrapper.handlerTest;


import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.handler.StackHandler;
import backend.academy.scrapper.linkConditions.LinkType;
import backend.academy.scrapper.services.StackOverflowService;
import backend.academy.scrapper.utils.LinkSourceUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = StackHandler.class)
class QuestionTest {

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.STACKOVERFLOW)
        .url("https://stackoverflow.com/questions/24840667")
        .checkedAt(CHECKED_AT)
        .build();
    static MockedStatic<LinkSourceUtil> linkSourceUtilMock;
    @Autowired
    private StackHandler question;
    @MockitoBean
    private StackOverflowService stackoverflowService;

    @BeforeAll
    public static void init() {
        linkSourceUtilMock = mockStatic(LinkSourceUtil.class);
        linkSourceUtilMock.when(() -> LinkSourceUtil.getDomain(any())).thenReturn("stackoverflow.com");
    }

    @AfterAll
    public static void close() {
        linkSourceUtilMock.close();
    }


    @Nested
    class GetLinkUpdateTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            doReturn(Optional.of("question was updated"))
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));
            doReturn(Optional.of("new answers"))
                .when(stackoverflowService)
                .getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));

            Optional<String> update = question.getLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService).getQuestionAnswersResponse("24840667", CHECKED_AT);
            assertThat(update).isPresent();
            assertThat(update.get()).isEqualTo("question was updated:\nnew answers");
        }

        @Test
        void shouldReturnEmptyWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));

            Optional<String> update = question.getLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService, never()).getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));
            assertThat(update).isEmpty();
        }
    }
}
