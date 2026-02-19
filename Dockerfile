FROM eclipse-temurin:17-jdk
LABEL authors="diego.munoz"

COPY build/libs/vitalitsoft-auth-service-1.0.0.jar app.jar

EXPOSE 8081

# Permite pasar el perfil activo y otras variables de entorno
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]
