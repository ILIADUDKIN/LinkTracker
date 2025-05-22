package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommitDto(
    Commit commit,
    @JsonProperty("html_url") String htmlUrl,
    @JsonProperty("author") Author author
) {
    public String getResponseBulletPoint() {
        return String.format(
            "➜ %s\n👤 %s\n🔗 %s",
            commit.message,
            author != null ? author.login : "неизвестно",
            htmlUrl
        );
    }

    public record Commit(String message) {
    }

    public record Author(String login) {
    }
}


