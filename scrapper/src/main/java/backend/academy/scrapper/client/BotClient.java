package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface BotClient {

    @PostExchange("/updates")
    void postUpdate(@RequestBody LinkUpdate linkupdate);
}
