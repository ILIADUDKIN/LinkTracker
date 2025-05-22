package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record IssueDto(
    @JsonProperty("html_url") String htmlUrl,
    String title,
    @JsonProperty("updated_at") OffsetDateTime updatedAt,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    @JsonProperty("user") User user,
    String body
) {
    private static String preview(String body, int maxLength) {
        return body.length() <= maxLength ? body : body.substring(0, maxLength) + "...";
    }

    public String getResponseBulletPoint() {
        return String.format(
            "➜ %s\n👤 %s\n📅 %s\n📝 %s\n🔗 %s",
            title,
            user != null ? user.login : "неизвестно",
            createdAt != null ? createdAt.toString() : "неизвестно",
            body != null ? preview(body, 200) : "(нет описания)",
            htmlUrl
        );
    }

    public record User(String login) {
    }
}
