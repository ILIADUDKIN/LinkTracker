package backend.academy.bot.unknownCommandTest;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.AbstractCommand;
import backend.academy.bot.command.HelpCommand;
import backend.academy.bot.command.ListCommand;
import backend.academy.bot.command.StartCommand;
import backend.academy.bot.command.UntrackCommand;
import backend.academy.bot.stateMachine.StateMachine;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnknownCommandTest {

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private Map<String, AbstractCommand> commands;

    // Создаем моки команд
    @Mock
    private HelpCommand helpCommand;
    @Mock
    private ListCommand listCommand;
    @Mock
    private StartCommand startCommand;
    @Mock
    private UntrackCommand untrackCommand;

    @Test
    void waitForCommand_UnknownCommand_At_Start_SendsErrorMessage() {
        // Arrange
        long chatId = 123L;
        String messageText = "/unknowncommand";
        when(helpCommand.getName()).thenReturn("/help");
        when(listCommand.getName()).thenReturn("/list");
        when(startCommand.getName()).thenReturn("/start");
        when(untrackCommand.getName()).thenReturn("/untrack");
        List<AbstractCommand> handlers = Arrays.asList(helpCommand, listCommand, startCommand, untrackCommand);
        StateMachine stateMachine = new StateMachine(telegramClient, handlers, scrapperClient, redisTemplate);
        // when(commands.containsKey(anyString())).thenReturn(false);

        // Act
        stateMachine.executeStateMachine(chatId, messageText);

        // Assert
        verify(telegramClient).sendMessage(eq(chatId), eq("Введите /start для начала работы с ботом."));
    }


    @Test
    void waitForCommand_UnknownCommand_At_CommandWaiting_SendsErrorMessage() {
        // Arrange
        long chatId = 123L;
        String startText = "/start";
        String messageText = "/unknowncommand";
        when(helpCommand.getName()).thenReturn("/help");
        when(listCommand.getName()).thenReturn("/list");
        when(startCommand.getName()).thenReturn("/start");
        when(untrackCommand.getName()).thenReturn("/untrack");
        List<AbstractCommand> handlers = Arrays.asList(helpCommand, listCommand, startCommand, untrackCommand);
        StateMachine stateMachine = new StateMachine(telegramClient, handlers, scrapperClient, redisTemplate);
        stateMachine.executeStateMachine(chatId, startText);

        // Act
        stateMachine.executeStateMachine(chatId, messageText);


        // Assert
        verify(telegramClient).sendMessage(eq(chatId), eq("Такой команды нет. Введите /help для получения списка всех возможных команд"));
    }
}

