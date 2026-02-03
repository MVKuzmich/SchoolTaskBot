# Production-grade Dockerfile для SchoolBot (мультимодульный Maven: bot-core + school-bot).
#
# Сборка: из корня репозитория
#   docker build -t schoolbot:latest .
#   docker compose up --build
#
# BuildKit обязателен для кэша Maven (--mount=type=cache).

# ============================================================================
# STAGE 1: Builder — сборка приложения
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Копируем всё дерево (POM + исходники) для корректной мультимодульной сборки
COPY pom.xml .
COPY bot-core bot-core
COPY school-bot school-bot

# Сборка в два шага: сначала bot-core в репозиторий, затем school-bot по его pom.xml —
# так spring-boot-maven-plugin гарантированно создаёт исполняемый (fat) JAR
RUN --mount=type=cache,target=/root/.m2 \
    mvn install -pl bot-core -am -B -ntp -q -Dmaven.test.skip=true \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=warn
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -f school-bot/pom.xml -DskipTests \
    -B -ntp -q \
    -Dmaven.test.skip=true \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=warn

# Верификация: в JAR должен быть Main-Class (fat JAR), иначе сборка падает здесь
RUN jar xf /app/school-bot/target/school-bot-1.0.0-SNAPSHOT.jar META-INF/MANIFEST.MF \
    && grep -q "Main-Class" META-INF/MANIFEST.MF \
    || (echo "ERROR: Main-Class not found in manifest (thin JAR). Check spring-boot-maven-plugin." && exit 1)

# ============================================================================
# STAGE 2: Runtime — финальный образ
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl ca-certificates

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Копируем именно исполняемый JAR (Spring Boot repackage); явное имя — чтобы не захватить .jar.original
COPY --from=builder /app/school-bot/target/school-bot-1.0.0-SNAPSHOT.jar app.jar

# JVM: учёт лимитов контейнера, G1GC
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Health check через Spring Actuator (должен быть включён в приложении)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
