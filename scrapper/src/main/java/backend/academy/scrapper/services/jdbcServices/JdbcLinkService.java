package backend.academy.scrapper.services.jdbcServices;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.linkConditions.LinkStatus;
import backend.academy.scrapper.reps.ChatLinkRepo;
import backend.academy.scrapper.reps.LinkRepo;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.utils.LinkParser;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessTypeConfig.class)
@Primary
public class JdbcLinkService implements LinkService {

    private final LinkRepo linkRepo;
    private final ChatLinkRepo chatLinkRepo;
    private final JdbcChatService chatService;

    @Transactional
    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        return linkRepo.findAllWithStatusAndOlderThan(
            LinkStatus.ACTIVE,
            OffsetDateTime.now().minusMinutes(minutes), limit
        );
    }

    @Transactional
    @Override
    public void updateLinkStatus(Link link, LinkStatus status) {
        linkRepo.
            updateStatus(link, status);
    }

    @Transactional
    @Override
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        linkRepo.updateCheckedAt(link, checkedAt);
    }

    @Override
    public LinkResponse addLinkToChat(Long chatId, String url, List<String> tags, List<String> filters) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForAdding(parsedLink, chat);
        chatLinkRepo.addLink(chat, link);
        return new LinkResponse(link.id(), link.url(), tags, filters);
    }

    private boolean canLinkBeAdded(Link link, Chat chat) {
        if (chatLinkRepo.isLinkAdded(chat, link)) {
            throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(link.url());
        }
        return !chatLinkRepo.isLinkAdded(chat, link);
    }

    @Transactional
    @Override
    public LinkResponse removeLinkFromChat(Long chatId, String url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = linkRepo.findByUrl(parsedLink.url())
            .filter(it -> chatLinkRepo.isLinkAdded(chat, it))
            .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(parsedLink.url()));
        chatLinkRepo.removeLink(chat, link);
        return new LinkResponse(link.id(), link.url(), Collections.emptyList(), Collections.emptyList());
    }

    @Transactional
    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        Chat chat = chatService.findByChatId(chatId);
        List<LinkResponse> links =
            linkRepo.findAllByChat(chat).stream()
                .map(link -> new LinkResponse(link.id(), link.url(), Collections.emptyList(), Collections.emptyList()))
                .toList();
        return new ListLinksResponse(links, links.size());
    }

    private Link processLinkForAdding(Link parsedLink, Chat chat) {
        return linkRepo.findByUrl(parsedLink.url())
            .filter(it -> canLinkBeAdded(it, chat))
            .orElseGet(() -> linkRepo.save(parsedLink));
    }
}
