package backend.academy.scrapper.services.jpaServices;


import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.linkConditions.LinkStatus;
import backend.academy.scrapper.reps.jpa.JpaLinkRepository;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.utils.LinkParser;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JPAAccessTypeConfig.class)
@Service
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatService chatService;

    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        return linkRepository.findAllWithStatusAndOlderThan(
            LinkStatus.ACTIVE,
            OffsetDateTime.now().minusMinutes(minutes),
            PageRequest.of(0, limit)
        );
    }

    @Transactional
    @Override
    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.id(), status.name());
        link.status(status);
    }

    @Transactional
    @Override
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.id(), checkedAt);
        link.checkedAt(checkedAt);
    }

    @Transactional
    @Override
    public LinkResponse addLinkToChat(Long chatId, String url, List<String> tags, List<String> filters) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForAdding(parsedLink, chat);
        chat.addLink(link);
        log.debug("add link{id={}} to chat{id={}}", link.id(), chat.id());
        return new LinkResponse(link.id(), link.url(), tags, filters);
    }

    @Transactional
    @Override
    public LinkResponse removeLinkFromChat(Long chatId, String url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForDeletion(parsedLink, chat);
        chat.removeLink(link);
        log.debug("remove link{id={}} from chat{id={}}", link.id(), chat.id());
        return new LinkResponse(link.id(), link.url(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        List<LinkResponse> trackedLinks = chatService.findByChatId(chatId)
            .links().stream()
            .map(link -> new LinkResponse(link.id(), link.url(), Collections.emptyList(), Collections.emptyList()))
            .toList();
        return new ListLinksResponse(trackedLinks, trackedLinks.size());
    }

    private Link processLinkForAdding(Link parsedLink, Chat chat) {
        chat.findLinkByUrl(parsedLink.url())
            .ifPresent(it -> {
                throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(it.url());
            });
        return linkRepository
            .findByUrl(parsedLink.url())
            .orElseGet(() -> linkRepository.save(parsedLink));
    }

    private Link processLinkForDeletion(Link parsedLink, Chat chat) {
        return chat.findLinkByUrl(parsedLink.url())
            .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(parsedLink.url()));
    }
}
