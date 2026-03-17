# -------- Stage 1: Build --------
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Copiar todos los módulos (necesarios para la compilación)
COPY domain ./domain
COPY application ./application
COPY infrastructure ./infrastructure
COPY bootstrap ./bootstrap

# Dar permisos y generar el JAR
RUN chmod +x gradlew && ./gradlew :bootstrap:bootJar --no-daemon

# -------- Stage 2: Run --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/bootstrap/build/libs/bootstrap.jar auth-service.jar

# Expone el puerto (puede configurarse vía ENV)
EXPOSE 8081

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
