# HW5

Учебный Java-проект для проверки разных типов хранилищ данных из Java/Spring.

В проекте проверяются:

- PostgreSQL через JDBC
- PostgreSQL через Spring Data JPA
- ClickHouse через JDBC
- Redis как key-value хранилище и кеш
- MongoDB через Spring Data MongoDB
- MinIO как S3-совместимое объектное хранилище
- Hazelcast как распределенное in-memory хранилище

## Требования

- Java 21+
- Docker Desktop
- IntelliJ IDEA или PowerShell

Gradle отдельно ставить не нужно: используется `gradlew.bat`.

## Запуск инфраструктуры

Из корня проекта:

```powershell
docker compose up -d
```

Проверить контейнеры:

```powershell
docker compose ps
```

Должны быть запущены:

- `postgres`
- `clickhouse`
- `redis`
- `s3`
- `mongo`
- `hazelcast`

## Порты и доступы

| Хранилище | Порт | Логин | Пароль |
| --- | --- | --- | --- |
| PostgreSQL | `15432` | `admin` | `password` |
| ClickHouse HTTP | `8123` | `admin` | `password` |
| ClickHouse native | `9000` | `admin` | `password` |
| Redis | `6379` | нет | нет |
| MinIO API | `9090` | `minioAccessKey` | `minioSecretKey` |
| MinIO Console | `9091` | `minioAccessKey` | `minioSecretKey` |
| MongoDB | `27017` | `admin` | `password` |
| Hazelcast | `5701` | нет | нет |

PostgreSQL опубликован на `15432`, чтобы не конфликтовать с локальным PostgreSQL на стандартном порту `5432`.

MinIO Console:

```text
http://localhost:9091
```

## Проверка Java-кода

Скомпилировать проект:

```powershell
.\gradlew.bat compileJava
```

Запустить проверки:

```powershell
.\gradlew.bat runJdbc
.\gradlew.bat runJpa
.\gradlew.bat runJpaLocks
.\gradlew.bat runClickHouse
.\gradlew.bat runRedis
.\gradlew.bat runMongo
.\gradlew.bat runS3
.\gradlew.bat runHazelcast
```

## Что делает каждая проверка

| Команда | Что проверяет |
| --- | --- |
| `runJdbc` | Подключение к PostgreSQL через `JdbcTemplate`, SQL-запросы и транзакции |
| `runJpa` | Spring Data JPA repository, JPQL, native query, paging, specifications |
| `runJpaLocks` | JPA-блокировки на PostgreSQL |
| `runClickHouse` | Создание таблицы, вставку данных, количество строк и аналитический запрос в ClickHouse |
| `runRedis` | Spring Cache поверх Redis |
| `runMongo` | Сохранение и чтение документа MongoDB |
| `runS3` | Создание bucket, загрузку и чтение файла через MinIO/S3 |
| `runHazelcast` | Запись и чтение данных из Hazelcast `IMap` |

## Ожидаемые признаки успешной работы

- `runJdbc` выводит пользователей `Luci` и `Mark`
- `runJpa` выводит результаты запросов `findByName`, JPQL, native query и specifications
- `runJpaLocks` выводит объект `Address`
- `runClickHouse` выводит `Contingency: ...` и `Rows: ...`
- `runRedis` выводит `FirstRun`, `SecondRun`, `Evict`, `ThirdRun`
- `runMongo` выводит `Optional[Author(...)]`
- `runS3` печатает содержимое файла `build.gradle.kts`
- `runHazelcast` выводит `John Doe` и map с двумя записями

## Полезные команды

Остановить контейнеры:

```powershell
docker compose down
```

Пересоздать контейнеры:

```powershell
docker compose down
docker compose up -d
```

Проверить `docker-compose.yaml`:

```powershell
docker compose config --quiet
```

## Структура проекта

```text
src/main/java/org/example
  clickhouse/   пример ClickHouse
  hazelcast/    пример Hazelcast
  jdbc/         пример PostgreSQL JDBC
  jpa/          примеры Spring Data JPA и блокировок
  mongo/        пример MongoDB
  redis/        пример Redis и cache
  s3/           пример MinIO/S3

docker-compose.yaml
build.gradle.kts
```
