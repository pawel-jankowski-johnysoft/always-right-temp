FROM gradle:latest AS build
WORKDIR /workspace/app
COPY build.gradle settings.gradle ./
COPY src src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17.0.5_8-jre-focal as builder
COPY --from=build /workspace/app/build/libs/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
