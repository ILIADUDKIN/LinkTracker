package backend.academy.scrapper.handlerTest;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.utils.LinkSourceUtil;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public interface LinkUpdateHandler {
    String regex();

    Optional<String> getLinkUpdate(Link link);

    default MatchResult linkMatcher(Link link) {
        return Pattern.compile("https://" + LinkSourceUtil.getDomain(link.linkType()) + regex())
            .matcher(link.url())
            .results()
            .toList()
            .getFirst();
    }
}
