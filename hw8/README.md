# HW8: сервис с логами, метриками и трейсами

Проект показывает, как в Spring Boot сервисе добавить наблюдаемость к обычной бизнес-логике:

- в сервисе есть метод с несколькими `if/else` ветками;
- в каждой ветке пишется лог;
- в каждой ветке добавляются данные в trace для Jaeger;
- в каждой ветке увеличиваются Prometheus-метрики;
- в Grafana автоматически поднимается dashboard для бизнес-метрик;
- логи можно смотреть в Kibana, метрики в Prometheus/Grafana, трейсы в Jaeger.

## Что делает сервис

Сервис имитирует обработку бизнес-заявки. Например, это может быть заявка на покупку, заказ, платеж или расчет скидки. На вход сервис получает:

- `amount` - сумма заявки;
- `customerTier` - тип клиента.

На основе этих данных сервис принимает одно из четырех решений:

- отклонить заявку;
- отправить заявку на ручную проверку;
- применить скидку;
- обработать заявку стандартно.

Endpoint:

```text
GET /api/demo/decision?amount=6000&customerTier=regular
```

Пример ответа:

```json
{
  "decision": "discount",
  "message": "Discount approved for large request",
  "amount": 6000,
  "customerTier": "regular"
}
```

Поля ответа:

- `decision` - итоговое решение бизнес-логики;
- `message` - текстовое объяснение решения;
- `amount` - сумма, которую передали в запросе;
- `customerTier` - нормализованный тип клиента.

## Бизнес-правила

Логика находится в файле:

```text
src/main/java/org/example/service/DemoService.java
```

Правила работают так:

1. Если `amount < 0`, сервис возвращает `rejected`.

   Это означает, что заявка некорректная, потому что сумма отрицательная.

   Пример:

   ```text
   /api/demo/decision?amount=-1&customerTier=regular
   ```

   Ожидаемое решение:

   ```text
   rejected
   ```

2. Если клиент `vip` и `amount >= 10000`, сервис возвращает `manual_review`.

   Это означает, что заявка крупная и важная, поэтому ее нужно проверить вручную.

   Пример:

   ```text
   /api/demo/decision?amount=12000&customerTier=vip
   ```

   Ожидаемое решение:

   ```text
   manual_review
   ```

3. Если `amount >= 5000`, сервис возвращает `discount`.

   Это означает, что сумма достаточно большая, поэтому сервис одобряет скидку.

   Пример:

   ```text
   /api/demo/decision?amount=6000&customerTier=regular
   ```

   Ожидаемое решение:

   ```text
   discount
   ```

4. Во всех остальных случаях сервис возвращает `standard`.

   Это обычная обработка без скидки и без ручной проверки.

   Пример:

   ```text
   /api/demo/decision?amount=1000&customerTier=regular
   ```

   Ожидаемое решение:

   ```text
   standard
   ```

## Что фиксируется в наблюдаемости

Для каждой ветки сервис делает три вещи.

### 1. Логи

В логи пишется, какая ветка была выбрана.

Примеры сообщений:

```text
Decision branch rejected
Decision branch manual_review
Decision branch discount
Decision branch standard
```

Эти сообщения нужны, чтобы в Kibana было видно, какое бизнес-решение принял сервис.

### 2. Трейсы

Для обработки создается span:

```text
DemoService.processDecision
```

В span добавляются tags:

```text
business.amount
business.customer_tier
business.decision
```

Также добавляются events:

```text
negative_amount_rejected
vip_high_value_manual_review
discount_approved
standard_processing
```

Это нужно, чтобы в Jaeger было видно не только сам HTTP-запрос, но и конкретную ветку бизнес-логики.

### 3. Метрики

Сервис пишет бизнес-метрики:

```text
business_decision_requests_total
business_decision_duration_seconds
```

`business_decision_requests_total` показывает, сколько раз была выбрана каждая ветка.

Пример label:

```text
decision="discount"
customer_tier="regular"
```

`business_decision_duration_seconds` показывает время выполнения бизнес-логики.

Эти метрики используются в Grafana dashboard:

```text
grafana/dashboards/business-decisions.json
```

## Что входит в docker-compose

В `docker-compose.yaml` поднимаются:

- `app` - Spring Boot приложение;
- `db` - PostgreSQL;
- `prometheus` - сбор метрик;
- `grafana` - dashboard для метрик;
- `elasticsearch`, `logstash`, `kibana` - сбор и просмотр логов;
- `jaeger` - просмотр distributed tracing;
- `postgres-exporter` - метрики PostgreSQL;
- `pgadmin` - UI для PostgreSQL.

## Как запустить

Из корня проекта:

```powershell
docker compose up -d --build
```

Проверить контейнеры:

```powershell
docker compose ps
```

Все основные контейнеры должны быть в состоянии `running` или `healthy`.

Адреса:

- приложение: http://localhost:8080
- Grafana: http://localhost:3000
- Kibana: http://localhost:5601
- Jaeger: http://localhost:16686
- Prometheus: http://localhost:9090
- pgAdmin: http://localhost:5050

Grafana:

```text
login: admin
password: admin
```

## Как проверить, что приложение отвечает

Выполнить:

```powershell
Invoke-RestMethod "http://localhost:8080/api/demo/decision?amount=6000&customerTier=regular"
```

Должен вернуться JSON с:

```text
decision = discount
```

Также можно проверить health endpoint:

```text
http://localhost:8080/actuator/health
```

Ожидаемый статус:

```json
{
  "status": "UP"
}
```

## Как проверить все ветки бизнес-логики

Выполнить 4 запроса:

```powershell
Invoke-RestMethod "http://localhost:8080/api/demo/decision?amount=-1&customerTier=regular"
Invoke-RestMethod "http://localhost:8080/api/demo/decision?amount=12000&customerTier=vip"
Invoke-RestMethod "http://localhost:8080/api/demo/decision?amount=6000&customerTier=regular"
Invoke-RestMethod "http://localhost:8080/api/demo/decision?amount=1000&customerTier=regular"
```

Ожидаемые решения:

| Запрос | Ожидаемый `decision` |
|---|---|
| `amount=-1&customerTier=regular` | `rejected` |
| `amount=12000&customerTier=vip` | `manual_review` |
| `amount=6000&customerTier=regular` | `discount` |
| `amount=1000&customerTier=regular` | `standard` |

Если эти четыре ответа получены, бизнес-логика работает корректно.

## Как проверить метрики в Prometheus

Открыть:

```text
http://localhost:9090
```

Проверить счетчик решений:

```promql
business_decision_requests_total
```

После выполнения 4 тестовых запросов должны появиться серии с разными `decision`:

```text
decision="rejected"
decision="manual_review"
decision="discount"
decision="standard"
```

Проверить время обработки:

```promql
business_decision_duration_seconds_count
```

Можно также проверить метрики напрямую через приложение:

```text
http://localhost:8080/actuator/prometheus
```

В выдаче должны быть строки:

```text
business_decision_requests_total
business_decision_duration_seconds
```

## Как проверить dashboard в Grafana

Открыть:

```text
http://localhost:3000/d/business_decisions/business-decisions
```

Если прямая ссылка не открылась:

1. Открыть http://localhost:3000
2. Войти под `admin` / `admin`.
3. Перейти в `Dashboards`.
4. Найти dashboard `Business Decisions`.

На dashboard должны быть панели:

- `Business Decision Rate` - скорость запросов по веткам;
- `Business Decisions Total` - общее количество решений;
- `Business Decision Duration` - время выполнения бизнес-логики.

Если после запуска dashboard пустой, нужно выполнить тестовые запросы из раздела выше и подождать 15-30 секунд, пока Prometheus соберет новые метрики.

## Как проверить логи в Kibana

Открыть:

```text
http://localhost:5601
```

Дальше:

1. Перейти в `Discover`.
2. Создать data view для индексов Logstash, например:

   ```text
   logstash-*
   ```

3. Выполнить поиск:

   ```text
   "Decision branch"
   ```

В результатах должны быть сообщения:

```text
Decision branch rejected
Decision branch manual_review
Decision branch discount
Decision branch standard
```

Если логов нет:

1. Убедиться, что контейнеры `app`, `logstash`, `elasticsearch`, `kibana` запущены.
2. Повторить 4 тестовых запроса.
3. Подождать 30-60 секунд.
4. Обновить Discover.

## Как проверить трейсы в Jaeger

Открыть:

```text
http://localhost:16686
```

Дальше:

1. В поле `Service` выбрать `app`.
2. Нажать `Find Traces`.
3. Открыть trace для запроса `/api/demo/decision`.
4. Найти span:

   ```text
   DemoService.processDecision
   ```

Внутри span должны быть tags:

```text
business.amount
business.customer_tier
business.decision
```

И event, соответствующий выбранной ветке:

```text
negative_amount_rejected
vip_high_value_manual_review
discount_approved
standard_processing
```

Если трейсов нет:

1. Убедиться, что контейнер `jaeger` запущен.
2. Повторить запросы к `/api/demo/decision`.
3. Подождать несколько секунд.
4. Обновить поиск в Jaeger.

## Скриншоты

Скриншоты находятся в папке:

```text
screenshots/
```

Имена:

- `kibana-business-logs.png` - Kibana Discover с логами `Decision branch`;
- `grafana-business-decisions.png` - Grafana dashboard `Business Decisions`;
- `jaeger-business-trace.png` - Jaeger trace со span `DemoService.processDecision`.

## Быстрая проверка без Docker

Проверить, что проект компилируется:

```powershell
.\gradlew.bat test
```

Если `gradlew.bat` не запускается из-за странной кодировки пути, перенести проект в папку с обычным ASCII-путем, например:

```text
C:\projects\seminar8
```

И повторить:

```powershell
.\gradlew.bat test
```
