package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.LinkUpdate;

public interface UpdateSender {

    boolean send(LinkUpdate update);
}
