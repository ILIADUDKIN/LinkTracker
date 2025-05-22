package backend.academy.bot.sheduledPoller;

import backend.academy.bot.stateMachine.StateMachine;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class TelegramUpdatePoller {

    private final TelegramBot bot;
    private final StateMachine stateMachine;
    private final Logger logger = LoggerFactory.getLogger(TelegramUpdatePoller.class);
    private int lastUpdateId = 0;  //Используем int, так как в Telegram API update_id - integer

    public TelegramUpdatePoller(TelegramBot bot, StateMachine stateMachine) {
        this.bot = bot;
        this.stateMachine = stateMachine;
    }

    @Scheduled(fixedDelayString = "3000")
    public void pollUpdates() {
        try {
            GetUpdates request = new GetUpdates().offset(lastUpdateId + 1).timeout(30);
            GetUpdatesResponse response = bot.execute(request);

            if (response.isOk()) {
                response.updates().forEach(update -> {
                    try {
                        processUpdate(update);
                    } catch (Exception e) {
                        logger.error("Ошибка обработки update {}", update.updateId(), e);
                    }
                });
            } else {
                logger.error("Ошибка получения обновлений: {}", response.description());
            }
        } catch (Exception e) {
            logger.error("Ошибка при опросе обновлений", e);
        }
    }

    private void processUpdate(com.pengrad.telegrambot.model.Update update) {
        if (update.updateId() > lastUpdateId) {
            lastUpdateId = update.updateId();
        }

        if (update.message() != null && update.message().text() != null) {
            long chatId = update.message().chat().id();
            String text = update.message().text();
            logger.info("Получено сообщение из чата {}: {}", chatId, text);
            stateMachine.executeStateMachine(chatId, text);
        }
    }
}
