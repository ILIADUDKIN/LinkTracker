package backend.academy.scrapper.services;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.dto.QuestionAnswerDto;
import backend.academy.scrapper.dto.QuestionDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.linkConditions.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
public class StackOverflowService {

    private static final String QUESTION_ANSWER_URL = "https://stackoverflow.com/a/%s";
    private final StackOverflowClient stackoverflowClient;
    private final LinkService linkService;

    public Optional<String> getQuestionResponse(String id, Link link) {
        return Optional.of(stackoverflowClient.getQuestion(id))
            .map(QuestionDto::questions)
            .flatMap(questions -> {
                if (CollectionUtils.isEmpty(questions)) {
                    linkService.updateLinkStatus(link, LinkStatus.BROKEN);
                    return Optional.empty();
                } else {
                    return Optional.of(questions.getFirst());
                }
            })
            .filter(question -> question.updatedAt().isAfter(link.checkedAt()))
            .map(this::getQuestionResponseMessage);
    }

    public Optional<String> getQuestionAnswersResponse(String id, OffsetDateTime lastCheckedAt) {
        return Optional.of(stackoverflowClient.getQuestionAnswers(id))
            .map(QuestionAnswerDto::answers)
            .filter(answers -> !CollectionUtils.isEmpty(answers))
            .map(answers -> answers.stream()
                .filter(ans -> ans.updatedAt().isAfter(lastCheckedAt))
                .collect(Collectors.toList()))
            .map(this::getQuestionAnswersResponseMessage);
    }

    private String getQuestionResponseMessage(QuestionDto.Question question) {
        return String.format("✔ вопрос [%s] был обновлен", question.title());
    }

    private String getQuestionAnswersResponseMessage(List<QuestionAnswerDto.Answer> answers) {
        return answers.stream()
            .map(answer -> {
                String preview = stripHtml(answer.body()).substring(0, Math.min(200, answer.body().length()));
                return String.format(
                    "✔ новый ответ на вопрос [%s]\n" +
                        "👤 пользователь: %s\n" +
                        "⏰ создан: %s\n" +
                        "💬 %s...\n" +
                        "🔗 %s",
                    getQuestionTitle(answer.questionId()),  // см. следующий пункт
                    answer.owner() != null ? answer.owner().displayName() : "неизвестен",
                    answer.createdAt(),
                    preview,
                    String.format(QUESTION_ANSWER_URL, answer.id())
                );
            })
            .collect(Collectors.joining("\n\n"));
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").replaceAll("&.*?;", "");
    }

    private String getQuestionTitle(String questionId) {
        return Optional.of(stackoverflowClient.getQuestion(questionId))
            .map(QuestionDto::questions)
            .filter(qs -> !CollectionUtils.isEmpty(qs))
            .map(qs -> qs.getFirst().title())
            .orElse("[неизвестный вопрос]");
    }

}

