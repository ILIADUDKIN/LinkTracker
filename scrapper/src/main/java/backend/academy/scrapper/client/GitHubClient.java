package backend.academy.scrapper.client;


import backend.academy.scrapper.dto.CommitDto;
import backend.academy.scrapper.dto.IssueDto;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface GitHubClient {

    @GetExchange("/repos/{owner}/{repo}/commits")
    List<CommitDto> getRepoCommits(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
        @RequestHeader("Authorization") String token
    );

    @GetExchange("/repos/{owner}/{repo}/commits")
    List<CommitDto> getBranchCommits(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam("sha") String branch,
        @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since
    );

    @GetExchange("/repos/{owner}/{repo}/issues?state=all")
    List<IssueDto> getIssuesAndPulls(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
        @RequestHeader("Authorization") String authorizationHeader
    );

    @GetExchange("/repos/{owner}/{repo}/issues/{num}")
    IssueDto getIssue(
        @PathVariable String owner,
        @PathVariable String repo,
        @PathVariable String num,
        @RequestHeader("Authorization") String authorizationHeader);

    @GetExchange("/repos/{owner}/{repo}/pulls/{num}")
    IssueDto getPullRequest(
        @PathVariable String owner,
        @PathVariable String repo,
        @PathVariable String num,
        @RequestHeader("Authorization") String authorizationHeader);

}
