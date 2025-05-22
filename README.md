
# Project 3 
`Link Tracker` - бот для подписки на обновление вопросов в `StackOverflow` и репозиториев в `GitHub`. Используется `Java`, `Spring Boot`, `Hibernate`, `Redis`, `Kafka`. Сборка происходит с помощью Docker. Для миграций используется `liquebase`.

## Порядок запуска и текущая архитектура:

1. **Запуск компонентов:** Делаем сборку `jar` файлов с помощью `mvn clean package -DskipTests`. 
2. Запускаем `docker engine` и собираем проект через `docker compose build`.






