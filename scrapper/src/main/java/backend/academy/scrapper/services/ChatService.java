package backend.academy.scrapper.services;

import backend.academy.scrapper.entity.Chat;

public interface ChatService {

    Chat findByChatId(Long chatId);

    void registerChat(Long chatId);

    void deleteChat(Long chatId);
}
