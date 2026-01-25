# ===== build =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY . .
RUN mvn -q -DskipTests package

# ===== runtime =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# cria usuário
RUN useradd -m appuser

# cria dirs que precisam escrita e dá ownership pro appuser
RUN mkdir -p /app/logs /app/uploads \
  && chown -R appuser:appuser /app/logs /app/uploads

USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8443
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app/app.jar"]

