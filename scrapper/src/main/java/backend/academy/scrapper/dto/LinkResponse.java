package backend.academy.scrapper.dto;

import java.util.List;

public record LinkResponse(
    Long id,
    String link,
    List<String> tags,
    List<String> filters) {
};

