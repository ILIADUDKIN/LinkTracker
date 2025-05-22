package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.linkConditions.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkService {

    List<Link> getLinksToUpdate(Integer minutes, Integer limit);

    void updateLinkStatus(Link link, LinkStatus status);

    void updateCheckedAt(Link link, OffsetDateTime checkedAt);

    LinkResponse addLinkToChat(Long chatId, String url, List<String> tags, List<String> filters);

    LinkResponse removeLinkFromChat(Long chatId, String url);

    ListLinksResponse getChatLinks(Long chatId);
}
