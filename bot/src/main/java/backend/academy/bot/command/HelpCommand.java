package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends AbstractCommand {


    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public void execute(Long chatId, String message, TelegramClient telegramClient, ScrapperClient scrapperClient) {
        String helpMessage = "Доступные команды:\n" +
            "/start - Начать работу с ботом\n" +
            "/help - Получить список команд\n" +
            "/track {} - начать отслеживать ссылку\n" +
            "/untrack {} - перестать отслеживать ссылку\n" +
            "/list - получить отслеживаемые ссылки\n";
        telegramClient.sendMessage(chatId, helpMessage);
    }
}
