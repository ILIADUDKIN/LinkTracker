package backend.academy.scrapper.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DatabaseAccessConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
    public static class JdbcAccessTypeConfig {
        @PostConstruct
        public void init() {
            log.info("Database access JDBC configuration loaded");
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
    public static class JPAAccessTypeConfig {
        @PostConstruct
        public void init() {
            log.info("Database access JPA configuration loaded");
        }
    }

}
