package backend.academy.scrapper.utils;

import backend.academy.scrapper.config.ScrapperConfig;
import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import reactor.util.retry.RetrySpec;

@UtilityClass
@Slf4j
public class RetryUtil {

    public static ExchangeFilterFunction retryFilter(ScrapperConfig.RetryConfig retryConfig) {
        return (request, next) -> next.exchange(request)
            .flatMap(clientResponse -> Mono.just(clientResponse)
                .filter(response -> clientResponse.statusCode().isError())
                .flatMap(response -> clientResponse.createException())
                .flatMap(Mono::error)
                .thenReturn(clientResponse))
            .retryWhen(getRetry(retryConfig));
    }

    private static Retry getRetry(ScrapperConfig.RetryConfig retryConfig) {
        return switch (retryConfig.strategy()) {
            case FIXED -> fixedRetry(retryConfig);
            case LINEAR -> linearRetry(retryConfig);
            case EXPONENTIAL -> exponentialRetry(retryConfig);
        };
    }

    private static RetryBackoffSpec fixedRetry(ScrapperConfig.RetryConfig retryConfig) {
        RetryBackoffSpec backoff = Retry.fixedDelay(retryConfig.maxAttempts(), retryConfig.backoff());
        return configureRetryBackoffSpec(retryConfig, backoff);
    }

    private static RetrySpec linearRetry(ScrapperConfig.RetryConfig retryConfig) {
        return Retry.max(retryConfig.maxAttempts())
            .filter(retryErrorFilter(retryConfig.codes()))
            .doBeforeRetryAsync(rs -> {
                Duration nextBackoff = retryConfig.backoff().multipliedBy(rs.totalRetries() + 1);
                if (retryConfig.maxBackoff() != null && nextBackoff.compareTo(retryConfig.maxBackoff()) > 0) {
                    nextBackoff = retryConfig.maxBackoff();
                }
                log.info(
                    "retry #{} - backoff=[{}] : {}",
                    rs.totalRetries() + 1,
                    nextBackoff,
                    rs.failure().getMessage()
                );
                return Mono.delay(nextBackoff).then();
            })
            .onRetryExhaustedThrow((sp, rs) -> rs.failure());
    }

    private static RetryBackoffSpec exponentialRetry(ScrapperConfig.RetryConfig retryConfig) {
        RetryBackoffSpec backoff = Retry.backoff(retryConfig.maxAttempts(), retryConfig.backoff());
        if (retryConfig.maxBackoff() != null) {
            backoff = backoff.maxBackoff(retryConfig.maxBackoff());
        }
        return configureRetryBackoffSpec(retryConfig, backoff);
    }

    private static RetryBackoffSpec configureRetryBackoffSpec(
        ScrapperConfig.RetryConfig retryConfig,
        RetryBackoffSpec backoff
    ) {
        return backoff.filter(retryErrorFilter(retryConfig.codes()))
            .doBeforeRetry(rs -> log.info("retry #{} : {}", rs.totalRetries() + 1, rs.failure().getMessage()))
            .onRetryExhaustedThrow((sp, rs) -> rs.failure());
    }

    private static Predicate<Throwable> retryErrorFilter(List<Integer> codes) {
        return error -> error instanceof WebClientResponseException ex
            && codes.contains(ex.getStatusCode().value());
    }
}
