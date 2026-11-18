FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY . .
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/data && chown -R app:app /app

COPY --from=build --chown=app:app /workspace/target/homesweethomecare-backend-*.jar app.jar

USER app
EXPOSE 8080

ENV DB_PATH=/app/data/collected-data.db \
    PORT=8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl --fail --silent http://localhost:${PORT}/api/recaptcha/config >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
