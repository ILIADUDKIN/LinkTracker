package backend.academy.scrapper.reps;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.linkConditions.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


public interface LinkRepo {

    Link save(Link link);

    List<Link> findAllByChat(Chat chat);

    Optional<Link> findByUrl(String url);

    boolean updateStatus(Link link, LinkStatus status);

    boolean updateCheckedAt(Link link, OffsetDateTime checkedAt);

    List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Integer limit);
}
