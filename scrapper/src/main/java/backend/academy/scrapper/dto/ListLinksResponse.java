package backend.academy.scrapper.dto;

import java.util.List;

public record ListLinksResponse(
    List<LinkResponse> links,
    int size) {
};


