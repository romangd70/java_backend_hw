# Этап 1: собираем Java-приложение через Gradle.
FROM gradle:8.8-jdk21 AS build
WORKDIR /home/gradle/app

# Копируем файлы сборки и исходный код.
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src

# Собираем JAR-файл приложения.
RUN gradle clean build --no-daemon

# Этап 2: запускаем собранное приложение в легком JRE-образе.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем JAR из этапа сборки.
COPY --from=build /home/gradle/app/build/libs/*.jar app.jar

# Значение второго числа по умолчанию, если оно не передано аргументом.
ENV SECOND_NUMBER=2

# Запускаем приложение. Первый аргумент по умолчанию равен 5.
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["5"]
