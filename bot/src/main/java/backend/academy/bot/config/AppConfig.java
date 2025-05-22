package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record AppConfig(
    @NotEmpty
    String telegramToken,
    @NotNull
    Boolean useQueue,
    Map<String, KafkaTopic> kafkaTopics,
    KafkaRetry kafkaRetry,
    @NotNull
    Boolean useRedis
) {

    public enum RetryStrategy {
        FIXED, LINEAR, EXPONENTIAL
    }

    public record RetryConfig(@NotNull RetryStrategy strategy, @NotNull Integer maxAttempts,
                              @NotNull Duration backoff, Duration maxBackoff, @NotEmpty List<Integer> codes) {
    }

    public record LinkSource(@NotNull String domain, Map<String, String> regex) {
    }

    public record KafkaTopic(@NotNull String name, @NotNull Integer partitions, @NotNull Short replicas) {
    }

    public record KafkaRetry(@NotNull Integer maxAttempts, @NotNull Long backoff,
                             @NotNull Long maxBackoff, @NotNull Integer multiplier) {
    }
}
