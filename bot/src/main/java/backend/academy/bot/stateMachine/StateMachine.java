package backend.academy.bot.stateMachine;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.AbstractCommand;
import backend.academy.bot.dto.ApiError;
import backend.academy.bot.dto.LinkResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StateMachine {

    private final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final ScrapperClient scrapperClient;
    private final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final Map<String, AbstractCommand> commands = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;


    public StateMachine(TelegramClient telegramClient, List<AbstractCommand> handlers, ScrapperClient scrapperClient, RedisTemplate<String, String> redisTemplate) {
        this.telegramClient = telegramClient;
        this.scrapperClient = scrapperClient;
        for (AbstractCommand command : handlers) {
            commands.put(command.getName().toLowerCase(), command);
        }
        this.redisTemplate = redisTemplate;
    }

    public void executeStateMachine(long chatId, String messageText) {
        Conversation conversation = conversations.computeIfAbsent(chatId, id -> new Conversation());
        State state = conversation.state();

        if (state == State.START) {
            if (messageText.trim().equalsIgnoreCase("/start")) {
                conversation.state(State.COMMAND_WAITING);
                commands.get("/start").execute(chatId, messageText, telegramClient, scrapperClient);
            } else {
                telegramClient.sendMessage(chatId, "Введите /start для начала работы с ботом.");
            }
            return;
        }

        if (state == State.COMMAND_WAITING) {
            waitForCommand(chatId, messageText, conversation);
            return;
        }

        switch (state) {
            case LINK_WAITING:
                addLink(chatId, messageText, conversation);
                break;
            case DOES_USER_WAIT_INPUT_TAGS:
                askTag(chatId, messageText, conversation);
                break;
            case DOES_USER_WAIT_INPUT_FILTERS:
                askFilters(chatId, messageText, conversation);
                break;
            case USER_WAITING_FOR_TAGS:
                waitingForTags(chatId, messageText, conversation);
                break;
            case USER_WAITING_FOR_FILTERS:
                waitingForFilters(chatId, messageText, conversation);
                break;
            default:
                telegramClient.sendMessage(chatId, "Такой команды нет.");
                conversation.reset();
                logger.error("Пользователь ввёл недопустимое состояние");
        }

    }

    private void waitForCommand(long chatId, String messageText, Conversation conversation) {
        if (messageText.trim().toLowerCase().startsWith("/track")) {
            String[] msg = messageText.split(" ", 2);
            if (msg.length >= 2 && !msg[1].isBlank()) {
                addLink(chatId, msg[1], conversation);
            } else {
                conversation.state(State.LINK_WAITING);
                telegramClient.sendMessage(chatId, "Введите ссылку для отслеживания");
            }
        } else if (messageText.trim().equalsIgnoreCase("/help") || messageText.trim().equalsIgnoreCase("/list") || messageText.trim().toLowerCase().startsWith("/untrack")) {
            commands.get(messageText.trim().split(" ")[0]).execute(chatId, messageText, telegramClient, scrapperClient);
            conversation.reset();
        } else if (messageText.trim().equalsIgnoreCase("/start")) {
            telegramClient.sendMessage(chatId, "Вы уже начали работать с ботом");
        } else {
            telegramClient.sendMessage(chatId, "Такой команды нет. Введите /help для получения списка всех возможных команд");
        }
    }

    private void addLink(long chatId, String link, Conversation conversation) {
        conversation.state(State.DOES_USER_WAIT_INPUT_TAGS);
        conversation.link(link);
        telegramClient.sendMessage(chatId, "Хотите настроить теги?(да/нет)");
    }

    private void askTag(long chatId, String messageText, Conversation conversation) {
        if (messageText.trim().equalsIgnoreCase("нет")) {
            conversation.state(State.DOES_USER_WAIT_INPUT_FILTERS);
            telegramClient.sendMessage(chatId, "Вы хотите ввести фильтры? (да/нет)");
        } else if (messageText.trim().equalsIgnoreCase("да")) {
            conversation.state(State.USER_WAITING_FOR_TAGS);
            telegramClient.sendMessage(chatId, "Введите теги");
        } else {
            telegramClient.sendMessage(chatId, "Ответьте да или нет");
        }
    }

    private void askFilters(long chatId, String messageText, Conversation conversation) {
        if (messageText.trim().equalsIgnoreCase("нет")) {
            completeTrack(chatId, conversation, conversation.tags(), Collections.emptyList());
        } else if (messageText.trim().equalsIgnoreCase("да")) {
            conversation.state(State.USER_WAITING_FOR_FILTERS);
            telegramClient.sendMessage(chatId, "Введите фильтры");
        } else {
            telegramClient.sendMessage(chatId, "Ответьте да или нет");
        }
    }

    private void completeTrack(Long chatId, Conversation conversation, List<String> tags, List<String> filters) {
        String link = conversation.link();
        Mono<LinkResponse> responseMono = scrapperClient.addLink(chatId, link, tags, filters);
        responseMono.subscribe(
            _ -> {
                String cacheKey = "user:" + chatId;
                redisTemplate.delete(cacheKey);
                telegramClient.sendMessage(chatId, "Ссылка добавлена.");
            },
            error -> sendErrorInfo(chatId, error));
        conversation.reset();
    }

    private void sendErrorInfo(Long chatId, Throwable error) {
        String errorMessage;
        logger.error("Ошибка: " + ((ApiError) error).description());
        errorMessage = switch (error) {
            case ApiError apiError when apiError.description().contains("409") -> "Вы уже отслеживаете эту ссылку.";
            case ApiError apiError when apiError.description().contains("404") -> "Чат не найден.";
            case ApiError apiError when apiError.description().contains("400") ->
                "Вы ввели некорректную ссылку. Мы используем ссылки на GitHub, StackOverflow для отслеживания изменений в вопросах и репозиториях.";
            default -> "Ошибка на стороне сервера. Попробуйте позже.";
        };
        telegramClient.sendMessage(chatId, errorMessage);
    }

    private void waitingForTags(Long chatId, String messageText, Conversation conversation) {

        List<String> tags = Arrays.stream(messageText.trim().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .toList();
        conversation.tags(tags);
        conversation.state(State.DOES_USER_WAIT_INPUT_FILTERS);
        telegramClient.sendMessage(chatId, "Вы хотите ввести фильтры? (да/нет)");
    }

    private void waitingForFilters(Long chatId, String messageText, Conversation conversation) {
        List<String> filters = Arrays.stream(messageText.trim().split("\\s+"))
            .filter(s -> !s.isEmpty())
            .toList();
        conversation.filters(filters);
        completeTrack(chatId, conversation, conversation.tags(), conversation.filters());
    }

    public void notifyUser(Long chatId, backend.academy.bot.dto.LinkUpdate update) {
        String notificationMessage = "Обновление по ссылке: " + update.url() + "\n " + update.description();
        telegramClient.sendMessage(chatId, "Уведомление:\n" + notificationMessage);
        logger.info("Пользователь уведомлен об обновлении ссылок");
    }
}
