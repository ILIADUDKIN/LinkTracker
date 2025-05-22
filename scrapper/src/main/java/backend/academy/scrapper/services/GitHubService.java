package backend.academy.scrapper.services;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.CommitDto;
import backend.academy.scrapper.dto.IssueDto;
import backend.academy.scrapper.dto.RepositoryDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class GitHubService {

    private final GitHubClient githubClient;
    String githubToken;
    String authorizationHeader;

    public GitHubService(GitHubClient githubClient, ScrapperConfig scrapperConfig) {
        this.githubClient = githubClient;
        this.githubToken = scrapperConfig.githubToken();
        this.authorizationHeader = "Bearer " + githubToken;

    }

    public Optional<String> getRepoCommitsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {

        List<CommitDto> commits = githubClient.getRepoCommits(
            repository.owner(),
            repository.repo(),
            lastCheckedAt,
            authorizationHeader
        );
        return getCommitsResponse(commits);
    }

    public Optional<String> getBranchCommitsResponse(
        RepositoryDto repository,
        String branch,
        OffsetDateTime lastCheckedAt
    ) {
        List<CommitDto> commits = githubClient.getBranchCommits(
            repository.owner(),
            repository.repo(),
            branch,
            lastCheckedAt
        );
        return getCommitsResponse(commits);
    }

    public Optional<String> getIssuesAndPullsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        List<IssueDto> issuesAndPulls =
            githubClient.getIssuesAndPulls(repository.owner(), repository.repo(), lastCheckedAt, authorizationHeader);
        return Optional.of(issuesAndPulls)
            .filter(_ -> !CollectionUtils.isEmpty(issuesAndPulls))
            .map(this::getIssuesAndPullsResponseMessage);
    }

    public Optional<String> getIssueResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        IssueDto issue = githubClient.getIssue(repository.owner(), repository.repo(), num, authorizationHeader);
        return getBaseIssueResponse(issue, lastCheckedAt, true);
    }

    public Optional<String> getPullRequestResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        IssueDto pullRequest = githubClient.getPullRequest(repository.owner(), repository.repo(), num, authorizationHeader);
        return getBaseIssueResponse(pullRequest, lastCheckedAt, false);
    }

    private Optional<String> getCommitsResponse(List<CommitDto> commits) {
        return Optional.of(commits)
            .filter(_ -> !CollectionUtils.isEmpty(commits))
            .map(this::getCommitsResponseMessage);
    }

    private Optional<String> getBaseIssueResponse(IssueDto issue, OffsetDateTime lastCheckedAt, boolean isIssue) {
        return Optional.of(issue)
            .filter(it -> it.updatedAt().isAfter(lastCheckedAt))
            .map(it -> getBaseIssueResponseMessage(it, isIssue));
    }

    private String getCommitsResponseMessage(List<CommitDto> commits) {
        return "✔ новые коммиты по ссылке:\n"
            + commits.stream()
            .map(CommitDto::getResponseBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    private String getIssuesAndPullsResponseMessage(List<IssueDto> issues) {
        return "✔ обновления в pr, issues :\n"
            + issues.stream()
            .map(IssueDto::getResponseBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    private String getBaseIssueResponseMessage(IssueDto issue, boolean isIssue) {
        return String.format("➜ %s was updated [%s]", isIssue ? "issue" : "PR", issue.title());
    }
}
