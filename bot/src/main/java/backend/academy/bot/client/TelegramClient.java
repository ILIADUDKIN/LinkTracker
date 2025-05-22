package backend.academy.bot.client;

import backend.academy.bot.command.AbstractCommand;
import backend.academy.bot.config.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// сервис осуществляет работу с api телеграм
@Service
public class TelegramClient {

    private final BotConfig botConfig;
    private final Logger logger = LoggerFactory.getLogger(TelegramClient.class);
    private final TelegramBot telegramBot;

    public TelegramClient(BotConfig botConfig, List<AbstractCommand> commandList) {
        this.telegramBot = botConfig.telegramBot();
        this.botConfig = botConfig;
    }

    public void sendMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = telegramBot.execute(request);

        if (response.isOk()) {
            logger.info("Успешно отправлено сообщение в чат {}: {}", chatId, text);
        } else {
            logger.error("Ошибка отправки сообщения в чат {}: {}. Ошибка: {}", chatId, text, response.description());
        }
    }

}
