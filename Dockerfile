# --------------- 1) Build stage (빌드 환경) ---------
FROM gradle:8.14-jdk21 AS builder
WORKDIR /workspace
COPY gradle gradle
COPY gradlew .
COPY settings.gradle build.gradle ./
COPY . .
# 빌드 수행
RUN ./gradlew --no-daemon clean bootJar -x test

# --------------- 2) Runtime stage (실행 환경) ---------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 단계에서 만든 jar 파일을 가져옵니다. 
# *.jar로 설정하면 파일 이름이 바뀌어도 복사해옵니다.
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 보안을 위해 권장되는 설정
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENV TZ=Asia/Seoul
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]