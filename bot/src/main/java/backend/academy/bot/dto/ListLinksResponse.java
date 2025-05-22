package backend.academy.bot.dto;

import java.util.List;


public record ListLinksResponse(
    List<LinkResponse> links,
    int size) {
};
