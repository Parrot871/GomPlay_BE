# --------------- 1) Build stage ---------
FROM gradle:8.10-jdk17 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew .
COPY settings.gradle build.gradle ./
COPY . .
RUN ./gradlew --no-daemon clean bootJar -x test
# --------------- 2) Runtime stage ---------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -u 10001 appuser
COPY --from=builder /workspace/build/libs/app.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser
ENV TZ=Asia/Seoul
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]