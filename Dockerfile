# -------- Stage 1: Build --------
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

# Copiar archivos de Gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Copiar solo bootstrap, ya que tiene las dependencias a los otros módulos
COPY bootstrap ./bootstrap
# Generar el jar
RUN ./gradlew :bootstrap:bootJar --no-daemon
# Copia tu JAR generado por Gradle
COPY bootstrap/build/libs/bootstrap.jar auth-service.jar

# Expone el puerto (puede configurarse vía ENV)
EXPOSE 8081

# Define the command to run the application when the container starts

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "/app/auth-service.jar"]