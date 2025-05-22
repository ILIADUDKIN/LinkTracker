package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;

public abstract class AbstractCommand {
    public abstract String getName();

    public abstract void execute(Long chatId, String message, TelegramClient telegramClient, ScrapperClient scrapperClient);
}
