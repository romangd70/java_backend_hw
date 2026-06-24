# HW4: JDBC и JPA

Учебный проект для демонстрации работы с PostgreSQL через Spring JDBC и JPA/Hibernate.

В проекте есть две версии одних и тех же сценариев:

- `org.example.jdbc.JdbcTemplateMain` - примеры на `JdbcTemplate`
- `org.example.jpa.JpaExamplesMain` - те же примеры, воспроизведенные через JPA `EntityManager`

Дополнительно есть:

- `org.example.jpa.JpaMain` - примеры Spring Data JPA query methods, JPQL, native query, paging, specifications
- `org.example.jpa.LockMain` - примеры optimistic и pessimistic locking

## Требования

- Java 21+
- Docker Desktop
- Gradle Wrapper из проекта: `gradlew.bat`
- IntelliJ IDEA, если удобнее запускать отдельные `main`-классы из IDE

## База данных

PostgreSQL поднимается через Docker Compose.

```powershell
docker compose up -d postgres
```

Параметры подключения:

```text
URL:      jdbc:postgresql://localhost:15434/mydatabase
User:     admin
Password: password
```

Проверить контейнер:

```powershell
docker ps --filter name=seminar4_postgres
```

Остановить контейнеры:

```powershell
docker compose down
```

Остановить и удалить volume с данными:

```powershell
docker compose down -v
```

## Сборка

```powershell
.\gradlew.bat compileJava
```

Полная сборка:

```powershell
.\gradlew.bat build
```

## Запуск JPA-примеров

Основная команда:

```powershell
.\gradlew.bat runJpaExamples
```

Она запускает `org.example.jpa.JpaExamplesMain`.

В `JpaExamplesMain.main()` все сценарии перечислены рядом. Для чистой проверки оставляй раскомментированным один пример за раз:

```java
phantomRead(entityManager, transactionManager);

// propagationRequiredNew(entityManager, transactionManager);
```

После изменения активного примера снова запуск:

```powershell
.\gradlew.bat runJpaExamples
```

В начале и в конце запуска таблица `users` очищается, чтобы старые строки не мешали читать результат. Значения `id` при этом могут продолжать расти - это нормальное поведение PostgreSQL sequence.

## Какие JDBC-примеры воспроизведены в JPA

В `JpaExamplesMain` есть JPA-версии следующих сценариев из `JdbcTemplateMain`:

- `insertUser` - вставка одной записи
- `insertMultipleUsers` - вставка нескольких записей
- `insertWithTransaction` - несколько операций в одной транзакции
- `insertWithTransactionRollback` - rollback транзакции
- `testReadNotCommited` - чтение во время незавершенной транзакции
- `notDirtyRead` - отсутствие dirty read при обычной изоляции
- `dirtyRead` - попытка dirty read через `READ_UNCOMMITTED`
- `repeatableRead` - пример повторного чтения
- `notRepeatableRead` - защита через `REPEATABLE_READ`
- `phantomRead` - phantom read
- `notPhantomRead` - защита от phantom read через `REPEATABLE_READ`
- `anomalyExample` - write skew/anomaly example на таблице `calculator`
- `notAnomalyExample` - защита через `SERIALIZABLE`
- `propagationRequired` - propagation `REQUIRED`
- `propagationSupport` - аналог сценария `SUPPORTS`
- `propagationRequiredNew` - propagation `REQUIRES_NEW`

## Как читать результат

Признаки успешного запуска:

```text
BUILD SUCCESSFUL
Initialized JPA EntityManagerFactory
Hibernate: insert into ...
Hibernate: select ...
```

Для `phantomRead` ожидаемый смысл вывода:

```text
Luci
Luci, Mark
```

Это показывает, что во время второй выборки появилась строка, вставленная параллельной транзакцией.

Для `notPhantomRead` оба чтения должны показывать один и тот же набор данных, потому что используется `REPEATABLE_READ`.

## Запуск JDBC-примеров

JDBC-примеры находятся в:

```text
src/main/java/org/example/jdbc/JdbcTemplateMain.java
```

Их удобнее запускать из IntelliJ IDEA: открыть класс и нажать Run у метода `main`.

Как и в JPA-версии, внутри `main()` лучше оставлять активным один пример за раз.

## PgAdmin

При необходимости можно поднять PgAdmin:

```powershell
docker compose up -d pgadmin
```

Открыть:

```text
http://localhost:5050
```

Логин:

```text
admin@admin.com
```

Пароль:

```text
password
```

Для подключения к базе из PgAdmin внутри Docker-сети:

```text
Host:     postgres
Port:     5432
Database: mydatabase
Username: admin
Password: password
```