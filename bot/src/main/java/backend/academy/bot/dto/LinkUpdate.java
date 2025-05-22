package backend.academy.bot.dto;

import java.util.List;

public record LinkUpdate(
    Long id,
    String url,
    String description,
    List<Long> tgChatIds) {
    public boolean isEmpty() {
        return url == null || description == null || tgChatIds == null;
    }
}

