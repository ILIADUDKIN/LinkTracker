package backend.academy.scrapper.services.jdbcServices;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.reps.ChatRepo;
import backend.academy.scrapper.services.ChatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(DatabaseAccessConfig.JdbcAccessTypeConfig.class)
@RequiredArgsConstructor
@Slf4j
public class JdbcChatService implements ChatService {

    private final ChatRepo chatRepo;

    @Override
    public Chat findByChatId(Long chatId) {
        return chatRepo.findByChatId(chatId)
            .orElseThrow(() -> ApiExceptionType.CHAT_NOT_FOUND.toException(chatId));
    }

    @Transactional
    @Override
    public void registerChat(Long chatId) {
        if (chatRepo.existsByChatId(chatId)) {
            throw ApiExceptionType.CHAT_ALREADY_EXISTS.toException(chatId);
        }
        Chat chat = Chat.builder().chatId(chatId).build();
        chatRepo.save(chat);
    }

    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        if (!chatRepo.existsByChatId(chatId)) {
            throw ApiExceptionType.CHAT_NOT_FOUND.toException(chatId);
        }
        chatRepo.delete(chatId);
    }
}
