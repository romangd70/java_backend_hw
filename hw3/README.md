# HW3: JPA, Hibernate, Spring Data JPA

Учебный проект на Java, демонстрирующий работу с таблицей `users` в PostgreSQL:

- описание таблицы через JPA-сущность;
- подключение к БД через Hibernate;
- выполнение выборок через HQL/JPQL, native SQL и Criteria API;
- подключение Spring Boot + Spring Data JPA;
- выполнение выборок через `JpaRepository`, derived queries и `@Query`.

## Технологии

- Java 21
- Gradle 8.8
- PostgreSQL 15
- Hibernate ORM 6.6
- Jakarta Persistence API
- Spring Boot 3.4
- Spring Data JPA
- Docker Compose

## Структура проекта

```text
src/main/java/org/example/
  User.java             # JPA-сущность таблицы users
  Main.java             # Hibernate/JPA demo: HQL, JPQL, native SQL, Criteria API
  SpringMain.java       # Spring Boot demo
  UserRepository.java   # Spring Data JPA repository
  UserService.java      # Сервис для работы с пользователями

src/main/resources/
  application.properties    # настройки Spring Data JPA
  hibernate.cfg.xml         # настройки Hibernate SessionFactory
  META-INF/persistence.xml  # настройки JPA EntityManager

migrations/
  V1__init_schema.sql   # создание users и начальные данные
  V2__add_row.sql       # добавление пользователя
```

## Таблица `users`

Таблица создается миграцией `migrations/V1__init_schema.sql`:

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

JPA-сущность находится в `src/main/java/org/example/User.java`.

## Запуск PostgreSQL

В проекте есть `docker-compose.yaml`. PostgreSQL проброшен на порт `15432`, чтобы не конфликтовать с локальной БД на стандартном порту `5432`.

```powershell
docker compose up -d postgres
```

Проверить, что контейнер запущен:

```powershell
docker ps
```

Параметры подключения:

```text
URL:      jdbc:postgresql://127.0.0.1:15432/mydatabase
Database: mydatabase
User:     admin
Password: password
```

## Применение SQL-миграций

После запуска PostgreSQL примените начальную схему и данные:

```powershell
Get-Content migrations\V1__init_schema.sql | docker exec -i seminar3_postgres psql -U admin -d mydatabase
Get-Content migrations\V2__add_row.sql | docker exec -i seminar3_postgres psql -U admin -d mydatabase
```

Проверить данные:

```powershell
docker exec seminar3_postgres psql -U admin -d mydatabase -c "select * from users;"
```

## Сборка

```powershell
.\gradlew.bat test
```

В проекте нет тестовых классов, поэтому команда используется как быстрая проверка компиляции.

## Hibernate/JPA demo

Запуск:

```powershell
.\gradlew.bat -q runHibernateDemo
```

Демонстрация выполняет:

- выборку всех пользователей через Hibernate `SessionFactory`;
- JPQL-запрос через `EntityManager`;
- native SQL-запрос через `EntityManager`;
- Criteria API-запрос по `username`.

Пример ожидаемого вывода:

```text
Hibernate SessionFactory: all users
ID: 1, Username: john_doe, Email: john_new@example.com
ID: 3, Username: jane_smith, Email: jane@example.com
```

## Spring Data JPA demo

Запуск:

```powershell
.\gradlew.bat -q runSpringDataDemo
```

Демонстрация выполняет:

- `findAll()`;
- `findByUsername("john_doe")`;
- `findByEmailContainingIgnoreCase("example.com")`;
- JPQL-запрос `findCreatedSince(...)` через `@Query`.

Основные классы:

- `UserRepository` расширяет `JpaRepository<User, Long>`;
- `UserService` содержит методы для выборки пользователей;
- `SpringMain` запускает Spring Boot-приложение и выводит результаты запросов.

## Полезные команды

Остановить контейнер:

```powershell
docker compose down
```

Остановить контейнер и удалить volume с данными:

```powershell
docker compose down -v
```

Повторно применить миграции после удаления volume:

```powershell
docker compose up -d postgres
Get-Content migrations\V1__init_schema.sql | docker exec -i seminar3_postgres psql -U admin -d mydatabase
Get-Content migrations\V2__add_row.sql | docker exec -i seminar3_postgres psql -U admin -d mydatabase
```

## Примечания

- Hibernate DDL auto отключен: `spring.jpa.hibernate.ddl-auto=none`.
- Схема БД должна создаваться SQL-миграциями из папки `migrations`.
- Если порт `15432` занят, измените порт в `docker-compose.yaml` и JDBC URL в:
  - `src/main/resources/application.properties`;
  - `src/main/resources/hibernate.cfg.xml`;
  - `src/main/resources/META-INF/persistence.xml`.
