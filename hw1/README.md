# ДЗ1

Учебный Java-проект для практики Markdown/Wiki-документации, Mermaid-диаграмм и Docker-сборки.

## Что есть в проекте

- [Resume.md](Resume.md) - микро-резюме в Markdown.
- [Resume.wiki](Resume.wiki) - микро-резюме в Wiki-разметке.
- [Flowchart.mmd](Flowchart.mmd) - блок-схема Mermaid.
- [Dockerfile](Dockerfile) - сборка и запуск Java-приложения в контейнере.
- [docker-compose.yaml](docker-compose.yaml) - пример запуска через Docker Compose.

## Локальная сборка

```bash
./gradlew build
```

Для Windows:

```bash
gradlew.bat build
```

## Запуск JAR

```bash
java -jar build/libs/Seminar1-1.0-SNAPSHOT.jar 5 2
```

## Сборка Docker-образа

```bash
docker build -t seminar1 .
```

## Запуск Docker-контейнера

```bash
docker run --rm -e SECOND_NUMBER=32 seminar1 10
```

Ожидаемый результат:

```text
First number: 10
Second number: 32
Your calculation: 42
```

## Запуск через Docker Compose

```bash
docker compose up --build
```
