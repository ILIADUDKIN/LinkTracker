package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.dto.ApiError;
import backend.academy.bot.dto.LinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UntrackCommand extends AbstractCommand {

    private final ScrapperClient scrapperClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String getName() {
        return "/untrack";
    }

    @Override
    public void execute(Long chatId, String message, TelegramClient telegramClient, ScrapperClient scrapperClient) {
        String[] split = message.split(" ");
        if (split.length < 2) {
            telegramClient.sendMessage(chatId, "Вы собираетесь удалить ссылку. Чтобы использовать эту команду," +
                "введите /untrack {ссылка} ");
            return;
        }
        String link = split[1];
        Mono<LinkResponse> linkResponseMono = this.scrapperClient.removeLink(chatId, link);
        linkResponseMono.subscribe(
            _ -> {
                String cacheKey = "user:" + chatId;
                redisTemplate.delete(cacheKey);
                telegramClient.sendMessage(chatId, "Ссылка удалена.");
            },
            error -> sendErrorInfo(chatId, error, message, telegramClient));
    }

    private void sendErrorInfo(Long chatId, Throwable error, String msg, TelegramClient telegramClient) {
        if (error instanceof ApiError apiError) {
            telegramClient.sendMessage(chatId, "Вы не отслеживаете эту ссылку. Введите /list для того, чтобы увидеть отслеживаемые ссылки.");
        } else {
            telegramClient.sendMessage(chatId, "Ошибка на стороне сервера. Попробуйте позже.");
        }
    }
}
