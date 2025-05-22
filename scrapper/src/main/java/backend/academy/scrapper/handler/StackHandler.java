package backend.academy.scrapper.handler;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.services.StackOverflowService;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class StackHandler implements LinkUpdateHandler {

    private final StackOverflowService stackoverflowService;
    @Value("${app.link-sources.stackoverflow.handlers.question.regex}")
    private String regex;

    @Override
    public String regex() {
        return regex;
    }

    @Override
    public Optional<String> getLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        String id = matcher.group("id");
        Optional<String> question = stackoverflowService.getQuestionResponse(id, link);
        Optional<String> answers = Optional.empty();
        if (question.isPresent()) {
            answers = stackoverflowService.getQuestionAnswersResponse(id, link.checkedAt());
        }
        return Stream.of(question, answers)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(StringUtils::hasText)
            .collect(Collectors.collectingAndThen(
                Collectors.joining(":\n"),
                it -> it.isEmpty() ? Optional.empty() : Optional.of(it)
            ));
    }
}
