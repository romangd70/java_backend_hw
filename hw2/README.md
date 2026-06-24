# HW2

Пример поднимает две независимые PostgreSQL в разных Docker-сетях, один pgAdmin,
отдельные Flyway-миграции для каждой БД и Java-приложение с двумя Hikari-пулами.

## Сервисы

- `postgres-db1`: `localhost:5432`, база `app_db1`, сеть `db1_network`
- `postgres-db2`: `localhost:5433`, база `app_db2`, сеть `db2_network`
- `pgadmin`: `http://localhost:5050`, логин `admin@admin.com`, пароль `password`
- `flyway-db1`: миграции из `migrations/db1`
- `flyway-db2`: миграции из `migrations/db2`
- `app`: подключается к обеим БД через Hikari и выполняет по два запроса

## Структура проекта

```text
Seminar2/
├── build.gradle.kts
├── docker-compose.yaml
├── Dockerfile
├── README.md
├── migrations/
│   ├── db1/
│   │   ├── V1__init_users.sql
│   │   ├── V2__insert_users.sql
│   │   └── V3__add_get_user_by_id.sql
│   └── db2/
│       ├── V1__init_products.sql
│       └── V2__insert_products.sql
├── pgadmin/
│   └── servers.json
└── src/
    └── main/
        └── java/
            └── org/
                └── example/
                    └── Main.java
```

- `docker-compose.yaml`: описывает две БД, разные сети, pgAdmin, Flyway и приложение.
- `migrations/db1`: Flyway-миграции для первой базы `app_db1`.
- `migrations/db2`: Flyway-миграции для второй базы `app_db2`.
- `pgadmin/servers.json`: преднастроенные подключения pgAdmin к обеим БД.
- `src/main/java/org/example/Main.java`: Java-код с двумя Hikari-пулами и SQL-запросами.
- `Dockerfile`: сборка и запуск Java-приложения внутри Docker.
- `build.gradle.kts`: зависимости PostgreSQL JDBC, HikariCP и настройка запуска приложения.

## Запуск через Docker

```bash
docker compose up --build
```

В pgAdmin уже добавлены оба сервера:

- `PostgreSQL DB1`: host `postgres-db1`, database `app_db1`
- `PostgreSQL DB2`: host `postgres-db2`, database `app_db2`

Пароль пользователя `admin` для обеих БД: `password`.

## Локальный запуск приложения

Сначала поднимите БД и миграции:

```bash
docker compose up -d postgres-db1 postgres-db2 flyway-db1 flyway-db2 pgadmin
```

Затем запустите Java-приложение с хоста:

```bash
gradlew.bat run
```

Локальные JDBC URL по умолчанию:

- `jdbc:postgresql://localhost:5432/app_db1`
- `jdbc:postgresql://localhost:5433/app_db2`
