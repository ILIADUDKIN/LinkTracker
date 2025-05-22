package backend.academy.scrapper.config;

import backend.academy.scrapper.linkConditions.LinkType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Validated
@EnableScheduling
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    @NotNull
    AccessType databaseAccessType,
    @NotNull
    Boolean useQueue,
    @NotNull
    Integer linkAge,

    @NotNull
    Integer linkUpdateBatchSize,

    @NotNull
    String githubToken,

    @NotNull
    LinkUpdaterScheduler linkUpdaterScheduler,
    GithubClient githubClient,

    @NotNull
    StackOverflowClient stackoverflowClient,

    @NotNull
    BotClient botClient,
    Map<String, KafkaTopic> kafkaTopics,
    Map<LinkType, LinkSource> linkSources

) {
    public enum RetryStrategy {
        FIXED, LINEAR, EXPONENTIAL
    }

    public enum AccessType {
        JDBC, JPA
    }

    public record LinkUpdaterScheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }

    public record LinkSource(@NotEmpty String domain, Map<String, LinkSourceHandler> handlers) {
    }

    public record LinkSourceHandler(@NotEmpty String regex, @NotEmpty String handler) {
    }

    public record RetryConfig(@NotNull RetryStrategy strategy, @NotNull Integer maxAttempts,
                              @NotNull Duration backoff, Duration maxBackoff, @NotEmpty List<Integer> codes) {
    }

    public record GithubClient(@DefaultValue("https://api.github.com") String api, RetryConfig retry) {
    }

    public record StackOverflowClient(@DefaultValue("https://api.stackexchange.com/2.3") String api,
                                      RetryConfig retry) {
    }

    public record BotClient(@NotNull String api, RetryConfig retry) {
    }

    public record KafkaTopic(@NotNull String name, @NotNull Integer partitions, @NotNull Short replicas) {
    }

}
