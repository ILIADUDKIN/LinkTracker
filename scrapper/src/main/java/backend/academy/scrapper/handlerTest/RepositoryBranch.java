package backend.academy.scrapper.handlerTest;

import backend.academy.scrapper.dto.RepositoryDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.services.GitHubService;
import java.util.Optional;
import java.util.regex.MatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RepositoryBranch implements LinkUpdateHandler {

    @Value("${app.link-sources.github.handlers.branch.regex}")
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
        String branch = matcher.group("branch");
        return githubService.getBranchCommitsResponse(repository, branch, link.checkedAt());
    }
}
