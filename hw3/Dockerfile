# Stage 1: Сборка приложения с использованием Gradle
FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/app

# Копируем build.gradle и src
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

# Собираем приложение
RUN gradle build

# Stage 2: Запуск приложения
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем fat jar из первого этапа
COPY --from=build /home/gradle/app/build/libs/*.jar app.jar

ENV SECOND_NUMBER=2

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["5"]