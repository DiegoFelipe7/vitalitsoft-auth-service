# Análisis de Arquitectura - VitalitSoft Auth Service

**Fecha de análisis:** 16 de Marzo de 2026  
**Versión del proyecto:** 1.0.0  
**Arquitectura base:** Clean Architecture / Hexagonal Architecture  
**Stack tecnológico:** Spring Boot 3.5.4, WebFlux, R2DBC, RabbitMQ, PostgreSQL

---

## 1. Resumen del Análisis

### 1.1 Visión General
El proyecto `vitalitsoft-auth-service` implementa una arquitectura **Clean Architecture** (también conocida como Arquitectura Hexagonal o Ports & Adapters) con una organización modular basada en Gradle multi-proyecto. La separación en módulos (`domain`, `application`, `infrastructure`, `bootstrap`) sigue los principios de inversión de dependencias y separación de responsabilidades.

### 1.2 Estructura de Módulos

```
vitalitsoft-auth-service/
├── domain/          → Entidades de negocio, interfaces de repositorios (ports)
├── application/     → Casos de uso, DTOs, mappers de aplicación
├── infrastructure/  → Adaptadores (entry points, driven adapters)
└── bootstrap/       → Punto de entrada, configuración de Spring Boot
```

### 1.3 Puntos Fuertes
- ✅ **Separación clara de capas** siguiendo Clean Architecture
- ✅ **Programación reactiva** consistente con Project Reactor (Mono/Flux)
- ✅ **Modelos de dominio ricos** con lógica de negocio encapsulada (ej. `AuthModel.ensureCanLogin()`)
- ✅ **Interfaces de repositorio bien definidas** en el dominio (ports)
- ✅ **Configuración externalizada** mediante variables de entorno
- ✅ **Manejo de errores centralizado** con `GlobalExceptionHandler`
- ✅ **Seguridad implementada** con JWT y Spring Security reactivo
- ✅ **Autenticación 2FA** con OTP
- ✅ **Documentación API** con Swagger/OpenAPI

---

## 2. Problemas Identificados

### 2.1 Problemas de Alta Prioridad 🔴

#### P1: DTOs en la Capa de Aplicación
**Ubicación:** `application/src/main/java/com/vitalitsoft/application/dto/`

**Problema:** Los DTOs de request/response están ubicados en la capa de aplicación, pero deberían estar en infraestructura ya que representan contratos de transporte HTTP.

**Impacto:** Viola el principio de Clean Architecture donde la capa de aplicación no debería conocer detalles de transporte.

```java
// Actualmente en application/dto/auth/request/LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email
    private String email;
    private String password;
}
```

#### P2: Casos de Uso retornan DTOs de Response
**Ubicación:** `application/usecase/auth/LoginUseCase.java`

**Problema:** Los casos de uso retornan `LoginResponse` (DTO) en lugar de modelos de dominio.

```java
public class LoginUseCase implements Function<LoginRequest, Mono<LoginResponse>> {
    // El caso de uso debería retornar TokenModel, no LoginResponse
}
```

#### P3: Mapper en Application usa lógica de infraestructura
**Ubicación:** `application/mapper/auth/AuthMapper.java`

**Problema:** El mapper de aplicación crea `LoginResponse` que es un DTO de transporte.

#### P4: Dockerfile con errores de construcción
**Ubicación:** `Dockerfile`

**Problema:** El Dockerfile intenta copiar el JAR antes de que se genere en la etapa de build.

```dockerfile
# ERROR: Orden incorrecto
RUN ./gradlew :bootstrap:bootJar --no-daemon
COPY bootstrap/build/libs/bootstrap.jar auth-service.jar  # ¡Ya se generó arriba!
```

#### P5: Falta de módulos necesarios en Dockerfile
**Ubicación:** `Dockerfile`

**Problema:** No se copian los módulos `domain`, `application`, `infrastructure` que son dependencias del módulo `bootstrap`.

### 2.2 Problemas de Media Prioridad 🟡

#### P6: Constantes HttpStatus duplicadas
**Ubicación:** `domain/shared/constants/HttpStatus.java`

**Problema:** Se define una clase `HttpStatus` personalizada cuando debería usarse enums o las constantes estándar de HTTP.

#### P7: JwtFilter con manejo de errores básico
**Ubicación:** `infrastructure/driven/adapters/security/jwt/filter/JwtFilter.java`

```java
if(auth == null) return Mono.error(new Throwable("no token was found"));
// Debería usar NexusException para consistencia
```

#### P8: Cookies sin SameSite configurado
**Ubicación:** `CookieManager.java`

**Problema:** Las cookies no configuran el atributo `SameSite`, lo cual es importante para protección CSRF.

```java
public static ResponseCookie createCookie(String name, String value) {
    return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(1))
            // Falta: .sameSite("Strict")
            .build();
}
```

#### P9: Group ID inconsistente en build.gradle del dominio
**Ubicación:** `domain/build.gradle`

```groovy
group = 'com.vitalitsoft.infrastructure'  // ERROR: debería ser com.vitalitsoft.domain
```

#### P10: AuthEntity implementa UserDetails innecesariamente
**Ubicación:** `infrastructure/driven/adapters/r2dbc/auth/AuthEntity.java`

**Problema:** La entidad de persistencia implementa `UserDetails` de Spring Security, mezclando responsabilidades.

### 2.3 Problemas de Baja Prioridad 🟢

#### P11: Logger inconsistente en JwtProvider
**Ubicación:** `infrastructure/driven/adapters/security/jwt/provider/JwtProvider.java`

```java
private static final Logger LOGGER = Logger.getLogger(JwtProvider.class.getName());
// Debería usar @Slf4j como en el resto del proyecto
```

#### P12: Falta de tests unitarios
**Ubicación:** Módulos `domain`, `application`, `infrastructure`

Los directorios de tests están vacíos.

#### P13: Nombre de aplicación incorrecto
**Ubicación:** `application.yaml`

```yaml
spring:
  application:
    name: "nexus-configuration-service"  # Debería ser vitalitsoft-auth-service
```

---

## 3. Riesgos y Limitaciones Actuales

### 3.1 Riesgos de Seguridad

| Riesgo | Severidad | Descripción |
|--------|-----------|-------------|
| Exposición de stack traces | Alta | `ErrorResponse` incluye `Arrays.toString(ex.getStackTrace())` en producción |
| JWT secret en variables de entorno | Media | Considerar uso de Vault o secrets manager |
| Falta de rate limiting | Media | No hay protección contra ataques de fuerza bruta |
| Cookies sin SameSite | Media | Vulnerabilidad potencial a CSRF |

### 3.2 Riesgos de Escalabilidad

| Riesgo | Severidad | Descripción |
|--------|-----------|-------------|
| Sesiones en BD | Media | RefreshTokens almacenados en PostgreSQL pueden ser cuello de botella |
| Sin caché | Media | No hay capa de caché para tokens o validaciones |

### 3.3 Riesgos de Mantenibilidad

| Riesgo | Severidad | Descripción |
|--------|-----------|-------------|
| DTOs acoplados a casos de uso | Alta | Cambios en API requieren cambios en application layer |
| Tests ausentes | Alta | Sin cobertura de tests, refactorizaciones son riesgosas |
| Documentación técnica limitada | Media | Falta ADRs y documentación de decisiones |

---

## 4. Recomendaciones de Mejora

### 4.1 Arquitectura

#### R1: Mover DTOs a Infrastructure (Alta Prioridad)

**Antes:**
```
application/
└── dto/
    └── auth/
        ├── request/
        └── response/
```

**Después:**
```
infrastructure/
└── entry/
    └── points/
        └── api/
            └── auth/
                ├── dto/
                │   ├── request/
                │   └── response/
                └── mapper/
```

#### R2: Casos de Uso deben retornar Modelos de Dominio

**Antes:**
```java
public class LoginUseCase implements Function<LoginRequest, Mono<LoginResponse>>
```

**Después:**
```java
public class LoginUseCase implements Function<LoginCommand, Mono<TokenModel>>
```

#### R3: Crear Commands en Application Layer

```java
// application/command/auth/LoginCommand.java
@Value
@Builder
public class LoginCommand {
    String email;
    String password;
}
```

### 4.2 Seguridad

#### R4: Eliminar stack traces en producción

```java
private ErrorResponse handleGenericException(Throwable ex) {
    String details = isProdProfile() ? "Error interno" : Arrays.toString(ex.getStackTrace());
    return ErrorResponse.of(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now(), details);
}
```

#### R5: Agregar SameSite a cookies

```java
public static ResponseCookie createCookie(String name, String value) {
    return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofDays(1))
            .build();
}
```

#### R6: Implementar Rate Limiting

Agregar `resilience4j` para rate limiting:

```java
@RateLimiter(name = "loginRateLimiter")
public Mono<LoginResponse> login(ServerRequest request)
```

### 4.3 Infraestructura

#### R7: Corregir Dockerfile

```dockerfile
# -------- Stage 1: Build --------
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
COPY domain ./domain
COPY application ./application
COPY infrastructure ./infrastructure
COPY bootstrap ./bootstrap

RUN chmod +x gradlew && ./gradlew :bootstrap:bootJar --no-daemon

# -------- Stage 2: Run --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/bootstrap/build/libs/bootstrap.jar auth-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "auth-service.jar"]
```

---

## 5. Propuestas de Refactorización

### 5.1 Refactorización de DTOs y Mappers

#### Paso 1: Crear estructura en infrastructure

```
infrastructure/entry/points/api/
├── auth/
│   ├── AuthHandler.java
│   ├── AuthRouterRest.java
│   ├── dto/
│   │   ├── LoginRequestDto.java
│   │   ├── LoginResponseDto.java
│   │   └── RegisterRequestDto.java
│   └── mapper/
│       └── AuthApiMapper.java
```

#### Paso 2: Crear Commands en application

```
application/
├── command/
│   └── auth/
│       ├── LoginCommand.java
│       ├── RegisterCommand.java
│       └── ActivateAccountCommand.java
├── usecase/
│   └── auth/
│       └── LoginUseCase.java
└── mapper/
    └── (Solo mappers dominio ↔ comando)
```

#### Paso 3: Actualizar flujo

```
HTTP Request → DTO → ApiMapper → Command → UseCase → Domain Model → ApiMapper → DTO → Response
```

### 5.2 Refactorización del Manejo de Errores

```java
// Crear enum para ambientes
public enum Environment { DEV, PROD }

// Inyectar perfil activo
@Value("${spring.profiles.active:prod}")
private String activeProfile;

private boolean isDevelopment() {
    return "dev".equalsIgnoreCase(activeProfile);
}

private ErrorResponse buildErrorResponse(Throwable ex) {
    String trace = isDevelopment() ? getStackTrace(ex) : null;
    // ...
}
```

### 5.3 Separar UserDetails de Entity

```java
// Nueva clase dedicada para Spring Security
public class AuthUserDetails implements UserDetails {
    private final AuthEntity entity;
    
    public AuthUserDetails(AuthEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(entity.getRole().name()));
    }
    // ...
}

// Entity limpia
@Table(name = "auth_users")
public class AuthEntity {
    @Id
    private UUID id;
    private String email;
    private String password;
    private Role role;
    private Status status;
    // Sin implementar UserDetails
}
```

---

## 6. Buenas Prácticas Sugeridas

### 6.1 Naming Conventions

| Tipo | Convención | Ejemplo |
|------|------------|---------|
| Use Cases | `{Acción}{Entidad}UseCase` | `LoginUserUseCase` |
| Commands | `{Acción}{Entidad}Command` | `LoginUserCommand` |
| DTOs | `{Entidad}{Request/Response}Dto` | `LoginRequestDto` |
| Mappers | `{Capa}{Entidad}Mapper` | `AuthApiMapper` |
| Repositories (port) | `{Entidad}Repository` | `AuthRepository` |
| Adapters | `{Entidad}{Tecnología}Adapter` | `AuthR2dbcAdapter` |

### 6.2 Estructura de Paquetes Recomendada

```
com.vitalitsoft/
├── domain/
│   └── auth/
│       ├── AuthModel.java
│       ├── TokenModel.java
│       └── port/
│           ├── AuthRepository.java
│           └── JwtRepository.java
├── application/
│   └── auth/
│       ├── command/
│       │   └── LoginCommand.java
│       ├── usecase/
│       │   └── LoginUseCase.java
│       └── mapper/
│           └── AuthCommandMapper.java
└── infrastructure/
    ├── adapter/
    │   ├── persistence/
    │   │   └── r2dbc/
    │   │       └── auth/
    │   └── security/
    │       └── jwt/
    └── entrypoint/
        └── rest/
            └── auth/
                ├── AuthHandler.java
                ├── dto/
                └── mapper/
```

### 6.3 Testing

```java
// Test de Caso de Uso
@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {
    @Mock private AuthRepository authRepository;
    @Mock private HashingRepository hashingRepository;
    @InjectMocks private LoginUseCase loginUseCase;
    
    @Test
    void shouldLoginSuccessfully() {
        // Given
        var command = LoginCommand.builder().email("test@test.com").password("pass").build();
        when(authRepository.findByEmail(anyString())).thenReturn(Mono.just(mockUser));
        
        // When
        var result = loginUseCase.apply(command);
        
        // Then
        StepVerifier.create(result)
            .assertNext(token -> assertThat(token.getAccessToken()).isNotNull())
            .verifyComplete();
    }
}
```

---

## 7. Ejemplos de Código Mejorado

### 7.1 LoginUseCase Refactorizado

```java
package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.application.command.auth.LoginCommand;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.port.AuthRepository;
import com.vitalitsoft.domain.auth.port.JwtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class LoginUseCase implements Function<LoginCommand, Mono<TokenModel>> {

    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final JwtRepository jwtRepository;

    @Override
    public Mono<TokenModel> apply(LoginCommand command) {
        log.info("Processing login for: {}", command.email());
        
        return authRepository.findByEmail(command.email())
                .doOnNext(user -> user.ensureCanLogin())
                .flatMap(user -> validatePassword(user, command.password()))
                .flatMap(this::generateToken)
                .doOnSuccess(token -> log.info("Login successful"))
                .doOnError(error -> log.warn("Login failed: {}", error.getMessage()));
    }

    private Mono<AuthModel> validatePassword(AuthModel user, String rawPassword) {
        return hashingRepository.matches(rawPassword, user.getPassword())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(AuthException.invalidPassword()))
                .thenReturn(user);
    }

    private Mono<TokenModel> generateToken(AuthModel user) {
        return jwtRepository.generateToken(
            user.getEmail(), 
            user.getRole().name(), 
            user.requiresTwoFactor()
        );
    }
}
```

### 7.2 Handler con Mapper en Infrastructure

```java
package com.vitalitsoft.infrastructure.entry.points.api.auth;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {
    private final LoginUseCase loginUseCase;
    private final AuthApiMapper authApiMapper;
    private final ObjectValidator objectValidator;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequestDto.class)
                .flatMap(objectValidator::validate)
                .map(authApiMapper::toCommand)           // DTO → Command
                .flatMap(loginUseCase)                   // Command → Domain
                .map(authApiMapper::toResponseDto)       // Domain → DTO
                .flatMap(response -> {
                    ResponseCookie cookie = CookieManager.createSecureCookie(
                        Constants.REFRESH_TOKEN_COOKIE_NAME, 
                        response.getRefreshToken()
                    );
                    return ServerResponse.ok()
                            .cookie(cookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}
```

### 7.3 CookieManager Mejorado

```java
public class CookieManager {

    private CookieManager() {
        throw new IllegalStateException("Utility class");
    }

    public static ResponseCookie createSecureCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(1))
                .build();
    }

    public static ResponseCookie createSecureCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
    }

    public static Mono<String> getCookieValue(ServerRequest request, String name) {
        return Mono.justOrEmpty(request.cookies().getFirst(name))
                .map(HttpCookie::getValue)
                .switchIfEmpty(Mono.error(AuthException.missingCookie(name)));
    }
}
```

---

## 8. Plan de Acción Recomendado

### Fase 1: Correcciones Críticas (1-2 días)
1. ✅ Corregir Dockerfile
2. ✅ Corregir group ID en domain/build.gradle
3. ✅ Corregir nombre de aplicación en application.yaml
4. ✅ Agregar SameSite a cookies

### Fase 2: Refactorización de DTOs (3-5 días)
1. Crear estructura de DTOs en infrastructure
2. Crear Commands en application
3. Actualizar Mappers
4. Actualizar Handlers

### Fase 3: Mejoras de Seguridad (2-3 días)
1. Eliminar stack traces en producción
2. Implementar rate limiting
3. Mejorar manejo de errores en JwtFilter

### Fase 4: Testing (5-7 días)
1. Tests unitarios para casos de uso
2. Tests de integración para adaptadores
3. Tests de seguridad

### Fase 5: Documentación (2-3 días)
1. Crear ADRs (Architecture Decision Records)
2. Documentar flujos de autenticación
3. Actualizar README

---

## 9. Conclusión

El proyecto presenta una **buena base arquitectónica** siguiendo Clean Architecture con Spring WebFlux. Los principales puntos de mejora se centran en:

1. **Separación más estricta de capas** - Mover DTOs a infrastructure
2. **Mejoras de seguridad** - Cookies, manejo de errores, rate limiting
3. **Infraestructura** - Dockerfile corregido
4. **Testing** - Implementar suite de tests

La implementación de estas mejoras incrementará significativamente la mantenibilidad, seguridad y escalabilidad del servicio.

---

*Documento generado por análisis de arquitectura automatizado*
