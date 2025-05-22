package backend.academy.scrapper.handlerTest;

import backend.academy.scrapper.dto.RepositoryDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.services.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.regex.MatchResult;

@RequiredArgsConstructor
@Component
public class PullRequest implements LinkUpdateHandler {

    @Value("${app.link-sources.github.handlers.pull-request.regex}")
    private String regex;
    private final GitHubService githubService;

    @Override
    public String regex() {
        return regex;
    }

    @Override
    public Optional<String> getLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));
        String num = matcher.group("num");
        return githubService.getPullRequestResponse(repository, num, link.checkedAt());
    }
}
