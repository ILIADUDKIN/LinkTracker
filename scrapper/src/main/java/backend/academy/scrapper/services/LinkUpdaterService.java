package backend.academy.scrapper.services;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.linkConditions.LinkStatus;
import backend.academy.scrapper.utils.LinkSourceUtil;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class LinkUpdaterService {

    private final LinkService linkService;
    private final UpdateSender updateSender;
    private final Map<String, LinkUpdateHandler> linkUpdateHandlers;
    @Value("${app.link-update-batch-size}")
    private Integer batchSize;
    @Value("${app.link-age}")
    private Integer linkAgeInMinutes;

    public LinkUpdaterService(
        LinkService linkService, UpdateSender updateSender,
        List<LinkUpdateHandler> linkUpdateHandlers
    ) {
        this.linkService = linkService;
        this.updateSender = updateSender;
        this.linkUpdateHandlers =
            linkUpdateHandlers.stream()
                .collect(Collectors.toMap(
                    it -> it.getClass().getCanonicalName(),
                    Function.identity()
                ));
    }

    public void updateLinks() {
        var updates = linkService.getLinksToUpdate(linkAgeInMinutes, batchSize);
        log.info("Вот это ссылки для обновления" + updates.toString());
        updates.forEach(this::processLinkUpdate);
    }

    private void processLinkUpdate(Link link) {
        Optional<LinkUpdateHandler> handler = LinkSourceUtil.getLinkSource(link.linkType())
            .flatMap(it -> getLinkUpdateHandler(link, it));
        if (handler.isEmpty()) {
            log.warn("no update handler: LinkType={}, link=[{}]", link.linkType(), link.url());
            return;
        }
        OffsetDateTime checkedAt = OffsetDateTime.now();
        try {
            handler.get().getLinkUpdate(link)
                .ifPresentOrElse(
                    it ->
                        notifyBot(link, it, checkedAt),
                    () ->
                        linkService.updateCheckedAt(link, checkedAt)
                );
        } catch (RuntimeException ex) {
            handleClientExceptionOnLinkUpdate(ex, link);
        }
    }

    private Optional<LinkUpdateHandler> getLinkUpdateHandler(Link link, ScrapperConfig.LinkSource linkSource) {
        return linkSource.handlers().values().stream()
            .filter(it -> Pattern.matches("https://" + linkSource.domain() + it.regex(), link.url()))
            .map(ScrapperConfig.LinkSourceHandler::handler)
            .map(linkUpdateHandlers::get)
            .filter(Objects::nonNull)
            .findFirst();
    }

    private void notifyBot(Link link, String message, OffsetDateTime checkedAt) {
        LinkUpdate update = new LinkUpdate(
            link.id(),
            link.url(),
            message,
            link.chats().stream()
                .map(Chat::chatId)
                .collect(Collectors.toList())
        );
        log.info(
            "send link update to the bot: id={}\nurl={}\nmessage={}",
            update.id(),
            update.url(),
            update.description()
        );
        boolean isSent = updateSender.send(update);
        if (isSent) {
            linkService.updateCheckedAt(link, checkedAt);
        }
    }

    private void handleClientExceptionOnLinkUpdate(RuntimeException ex, Link link) {
        log.info("client error on link update: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc) {
            HttpStatusCode status = clientExc.getStatusCode();
            if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST)) {
                linkService.updateLinkStatus(link, LinkStatus.BROKEN);
            }
        }
    }
}
