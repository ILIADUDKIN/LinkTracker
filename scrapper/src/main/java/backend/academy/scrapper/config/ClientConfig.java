package backend.academy.scrapper.config;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.utils.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@RequiredArgsConstructor
@Component
public class ClientConfig {

    private final ScrapperConfig scrapperConfig;

    @Bean
    public GitHubClient gitHubClient() {
        return buildHttpInterface(scrapperConfig.githubClient().api(), GitHubClient.class,
            scrapperConfig.githubClient().retry());
    }

    @Bean
    public StackOverflowClient stackoverflowClient() {
        return buildHttpInterface(scrapperConfig.stackoverflowClient().api(), StackOverflowClient.class,
            scrapperConfig.stackoverflowClient().retry());
    }

    @Bean
    public BotClient botClient() {
        return buildHttpInterface(scrapperConfig.botClient().api(), BotClient.class,
            scrapperConfig.botClient().retry());
    }


    private <T> T buildHttpInterface(String baseUrl, Class<T> serviceType, ScrapperConfig.RetryConfig retryConfig) {
        WebClientAdapter webClientAdapter = WebClientAdapter.create(
            WebClient.builder()
                .baseUrl(baseUrl)
                .filter(RetryUtil.retryFilter(retryConfig))
                .build());
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build().createClient(serviceType);
    }
}
