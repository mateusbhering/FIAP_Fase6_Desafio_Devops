# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Baixa dependencias em uma camada separada para aproveitar o cache do Docker
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
# Os testes rodam na etapa de CI do pipeline; aqui apenas empacotamos
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/ecotrack-api.jar extract --layers --launcher --destination target/extracted

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:24-jre
LABEL org.opencontainers.image.title="ecotrack-api" \
      org.opencontainers.image.description="API ESG de inventario de emissoes GHG" \
      org.opencontainers.image.licenses="MIT"

RUN groupadd --system ecotrack && useradd --system --gid ecotrack --no-create-home ecotrack \
 && mkdir -p /app/logs && chown -R ecotrack:ecotrack /app
WORKDIR /app

# Camadas do Spring Boot: dependencias mudam pouco, codigo da aplicacao muda sempre
COPY --from=build --chown=ecotrack:ecotrack /build/target/extracted/dependencies/ ./
COPY --from=build --chown=ecotrack:ecotrack /build/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=ecotrack:ecotrack /build/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=ecotrack:ecotrack /build/target/extracted/application/ ./

ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} \
    LOG_DIR=/app/logs \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

USER ecotrack
EXPOSE 8080
VOLUME ["/app/logs"]

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health/readiness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
