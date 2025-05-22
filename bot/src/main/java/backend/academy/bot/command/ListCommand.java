package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.dto.ApiError;
import backend.academy.bot.dto.ListLinksResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListCommand extends AbstractCommand {

    private final ScrapperClient scrapperClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    @Override
    public String getName() {
        return "/list";
    }

    @Override
    public void execute(Long chatId, String messageText, TelegramClient telegramClient, ScrapperClient scrapperClient) {
        String cacheKey = "user:" + chatId;

        // Попробовать получить из Redis
        String cached = redisTemplate.opsForValue().get(cacheKey);

        log.info("Cached links: {}", cached);

        if (cached != null) {
            // Есть в кэше
            try {
                ListLinksResponse cachedResponse = objectMapper.readValue(cached, ListLinksResponse.class);
                sendResponse(chatId, cachedResponse, telegramClient);
                return;
            } catch (JsonProcessingException e) {
                redisTemplate.delete(cacheKey);
            }
        }

        // Нет в кэше - запрашиваем у scrapper
        Mono<ListLinksResponse> monoResponse = this.scrapperClient.getLinks(chatId);
        monoResponse.subscribe(
            response -> {
                try {
                    String json = objectMapper.writeValueAsString(response);
                    redisTemplate.opsForValue().set(cacheKey, json);
                } catch (JsonProcessingException e) {
                    e.printStackTrace(); // Или лог
                }

                sendResponse(chatId, response, telegramClient);
            },
            error -> sendErrorInfo(chatId, error, "Ошибка, вы не получили ссылки", telegramClient)
        );
    }

    private void sendResponse(Long chatId, ListLinksResponse response, TelegramClient telegramClient) {
        if (response.links() == null || response.links().isEmpty()) {
            telegramClient.sendMessage(chatId, "Пока вы не отслеживаете ни одной ссылки");
        } else {
            StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n");
            response.links().forEach(link -> sb.append(link.link()).append("\n"));
            telegramClient.sendMessage(chatId, sb.toString());
        }
    }

    private void sendErrorInfo(Long chatId, Throwable error, String msg, TelegramClient telegramClient) {
        if (error instanceof ApiError apiError) {
            telegramClient.sendMessage(chatId, msg + ":\n " + apiError.description());
        } else {
            telegramClient.sendMessage(chatId, msg + ":\n " + error.getMessage());
        }
    }
}
