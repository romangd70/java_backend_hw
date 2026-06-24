# HW6

Учебный Spring Boot проект с примером авторизации через собственную HTML-форму. После ввода логина и пароля сервер не создает `JSESSIONID`, а выдает JWT-токен. Браузер сохраняет токен в `localStorage` и отправляет его в заголовке `Authorization`.

## Требования

- Java 21+
- Gradle Wrapper из проекта: `gradlew.bat`
- IntelliJ IDEA или другой IDE для Java

## Сборка

```powershell
.\gradlew.bat compileJava
```

Полная сборка:

```powershell
.\gradlew.bat build
```

## Запуск JWT-формы авторизации

Самый простой способ - открыть в IntelliJ IDEA класс:

```text
src/main/java/org/example/security/login_form/SecurityMain.java
```

и запустить метод `main`.

После запуска приложение будет доступно по адресу:

```text
http://localhost:8080/login.html
```

## Пользователи для входа

| Логин | Пароль | Роли |
| --- | --- | --- |
| `user` | `password` | `USER` |
| `admin` | `admin` | `USER`, `ADMIN` |

Пользователи задаются в `InMemoryUserDetailsManager`:

```text
src/main/java/org/example/security/login_form/BasicConfiguration.java
```

## Как работает авторизация

1. Пользователь открывает `login.html`.
2. Форма отправляет логин и пароль на `POST /login` в JSON-формате.
3. `AuthController` проверяет данные через `AuthenticationManager`.
4. Если логин и пароль верные, сервер возвращает JWT.
5. Браузер сохраняет токен в `localStorage` под ключом `jwt`.
6. При обращении к защищенным endpoint браузер отправляет токен:

```http
Authorization: Bearer eyJ...
```

7. `JwtFilter` проверяет подпись токена и добавляет пользователя в `SecurityContext`.

## Проверка через браузер

1. Запустить приложение.
2. Открыть:

```text
http://localhost:8080/login.html
```

3. Ввести логин `user` и пароль `password`.
4. После успешного входа откроется `home.html`.
5. На странице должно быть:

```text
JWT-токен принят сервером.
Вы вошли как USER.
```

6. В DevTools браузера открыть `Application -> Local Storage`.
7. Проверить, что есть ключ `jwt` со значением вида:

```text
Bearer eyJ...
```

## Проверка через PowerShell

Получить JWT:

```powershell
$response = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/login `
  -ContentType "application/json" `
  -Body '{"username":"user","password":"password"}'

$token = "$($response.tokenType) $($response.token)"
$token
```

Проверить защищенный endpoint:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/user `
  -Headers @{ Authorization = $token }
```

Ожидаемый ответ:

```text
Вы вошли как USER.
```

Без токена endpoint `/user` должен быть недоступен.

## Основные файлы JWT-авторизации

| Файл | Назначение |
| --- | --- |
| `src/main/resources/static/login.html` | HTML-форма входа, отправляет логин и пароль через `fetch` |
| `src/main/resources/static/home.html` | Проверяет сохраненный JWT и вызывает `/user` |
| `src/main/java/org/example/security/login_form/AuthController.java` | Endpoint `POST /login`, выдает JWT |
| `src/main/java/org/example/security/login_form/JwtFilter.java` | Проверяет `Authorization: Bearer ...` |
| `src/main/java/org/example/security/login_form/BasicConfiguration.java` | Настройка Spring Security без сессий |
| `src/main/java/org/example/security/login_form/SecurityController.java` | Примеры публичных и защищенных endpoint |
