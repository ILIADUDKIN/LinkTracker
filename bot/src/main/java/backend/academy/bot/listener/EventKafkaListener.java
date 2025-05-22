package backend.academy.bot.listener;


import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.stateMachine.StateMachine;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Validated
@Component
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class EventKafkaListener {

    private final StateMachine stateMachine;

    public EventKafkaListener(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @RetryableTopic(attempts = "${app.kafka-retry.max-attempts}",
        backoff = @Backoff(delayExpression = "${app.kafka-retry.backoff}",
            multiplierExpression = "${app.kafka-retry.multiplier}",
            maxDelayExpression = "${app.kafka-retry.max-backoff}"),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltTopicSuffix = "-dlq",
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        kafkaTemplate = "kafkaTemplate",
        exclude = {ValidationException.class})
    @KafkaListener(topics = "${app.kafka-topics.link-update.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Payload @Valid LinkUpdate update) {
        List<Long> chatIds = update.tgChatIds();
        for (Long chatId : chatIds) {
            stateMachine.notifyUser(chatId, update);
        }
    }
}
