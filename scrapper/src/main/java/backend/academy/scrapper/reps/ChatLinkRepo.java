package backend.academy.scrapper.reps;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;

public interface ChatLinkRepo {
    boolean isLinkAdded(Chat chat, Link link);

    boolean addLink(Chat chat, Link link);

    boolean removeLink(Chat chat, Link link);
}
