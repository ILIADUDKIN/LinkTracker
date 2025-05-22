package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.dto.ApiError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StartCommand extends AbstractCommand {


    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public void execute(Long chatId, String message, TelegramClient telegramClient, ScrapperClient scrapperClient) {
        Mono<Void> result = scrapperClient.registerChat(chatId)
            .doOnSuccess(unused -> telegramClient.sendMessage(chatId, "Добро пожаловать в бота! Напишите /help " +
                "для получения списка всех возможных команд."))
            .doOnError(error -> {
                if (error instanceof ApiError) {
                    if (((ApiError) error).description().contains("409"))
                        telegramClient.sendMessage(chatId, "Вы зарегистрированы в системе, простите, если вдруг мы об этом забыли :)");
                } else {
                    telegramClient.sendMessage(chatId, "Непредвиденная ошибка регистрации. Мы продолжаем работать, чтобы это починить.");
                }
            })
            .then();
        result.subscribe();
    }
}
