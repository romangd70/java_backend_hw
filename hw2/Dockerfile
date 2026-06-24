FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN gradle installDist

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /home/gradle/app/build/install/Seminar2 /app/Seminar2

ENTRYPOINT ["/app/Seminar2/bin/Seminar2"]
