package backend.academy.scrapper.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class KafkaConfig {

    private final ScrapperConfig applicationConfig;

    @Bean
    public KafkaAdmin.NewTopics topics() {
        List<NewTopic> topics = new ArrayList<>();
        applicationConfig.kafkaTopics().values()
            .forEach(topic ->
                topics.add(TopicBuilder
                    .name(topic.name())
                    .partitions(topic.partitions())
                    .replicas(topic.replicas())
                    .build()));
        return new KafkaAdmin.NewTopics(topics.toArray(NewTopic[]::new));
    }
}
