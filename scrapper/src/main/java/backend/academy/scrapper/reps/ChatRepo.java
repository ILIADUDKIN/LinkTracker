package backend.academy.scrapper.reps;

import backend.academy.scrapper.entity.Chat;
import java.util.Optional;

public interface ChatRepo {

    Chat save(Chat chat);

    boolean delete(Long chatId);

    Optional<Chat> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);

    Optional<Chat> findWithLinksByChatId(Long chatId);
}
