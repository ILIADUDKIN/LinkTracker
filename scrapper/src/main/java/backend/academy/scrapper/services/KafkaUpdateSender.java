package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.LinkUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class KafkaUpdateSender implements UpdateSender {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${app.kafka-topics.link-update.name}")
    private String topic;

    @Override
    public boolean send(LinkUpdate update) {
        try {
            kafkaTemplate.send(topic, mapper.writeValueAsString(update));
            return true;
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("Ошибка по топику: topic={}\n{}: {}", topic, ex.getMessage(), ex.getCause());
            return false;
        }
    }
}
