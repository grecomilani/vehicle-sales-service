# syntax=docker/dockerfile:1

# ---------- Build: compila e empacota com o Maven Wrapper ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Mirror opcional do Maven Central (ex.: proxy corporativo)
ARG MAVEN_MIRROR_URL=""

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN if [ -n "$MAVEN_MIRROR_URL" ]; then \
        mkdir -p /root/.m2 && printf '<settings><mirrors><mirror><id>mirror</id><url>%s</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' "$MAVEN_MIRROR_URL" > /root/.m2/settings.xml; \
        export MVNW_REPOURL="$MAVEN_MIRROR_URL"; \
    fi && \
    ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests && \
    cp target/*.jar app.jar && \
    java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ---------- Runtime: apenas a JRE e a aplicação ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# UID/GID numéricos: permitem ao Kubernetes verificar que o container não roda como root
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app --no-create-home app

# Camadas da aplicação, da que menos muda para a que mais muda
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER 10001:10001
EXPOSE 8081

# Usa a memória disponível no container em vez da memória do host
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
