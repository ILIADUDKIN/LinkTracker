package backend.academy.bot.dto;

import java.util.List;


public record LinkResponse(
    Long id,
    String link,
    List<String> tags,
    List<String> filters) {
};

