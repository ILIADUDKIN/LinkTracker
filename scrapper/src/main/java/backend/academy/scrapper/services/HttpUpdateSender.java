package backend.academy.scrapper.services;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "false")
public class HttpUpdateSender implements UpdateSender {

    private final BotClient botClient;

    public boolean send(LinkUpdate update) {
        try {
            botClient.postUpdate(update);
            return true;
        } catch (RuntimeException ex) {
            log.info("client error when sending an update: {}", ex.getMessage());
            if (ex instanceof WebClientResponseException clientExc
                && ArrayUtils.isNotEmpty(clientExc.getResponseBodyAsByteArray())) {
                log.info("response: {}", clientExc.getResponseBodyAsString());
            }
            return false;
        }
    }
}
