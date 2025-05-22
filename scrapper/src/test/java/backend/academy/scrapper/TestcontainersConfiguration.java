package backend.academy.scrapper;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

// isolated from the "bot" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }

//    @Bean
//    @RestartScope
//    @ServiceConnection
//    PostgreSQLContainer<?> postgresContainer() {
//        return new PostgreSQLContainer<>("postgres:17-alpine")
//                .withExposedPorts(5432)
//                .withDatabaseName("local")
//                .withUsername("postgres")
//                .withPassword("test");
//    }
//
//    @Bean
//    @RestartScope
//    @ServiceConnection
//    KafkaContainer kafkaContainer() {
//        return new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
//    }
}
