# VitalitSoft Auth Service - Instrucciones para GitHub Copilot

## Análisis de la Arquitectura del Proyecto

### Estructura General
Este proyecto sigue una **Arquitectura Hexagonal (Clean Architecture)** con las siguientes capas:

```
vitalitsoft-auth-service/
├── domain/           # Capa de dominio (núcleo del negocio)
├── application/      # Casos de uso y lógica de aplicación
├── infrastructure/   # Adaptadores externos (DB, API, Security)
├── bootstrap/        # Punto de entrada y configuración de Spring Boot
```

## Patrones Arquitectónicos Identificados

### 1. **Arquitectura Hexagonal (Ports & Adapters)**
- **Domain**: Entidades, modelos y interfaces (gateways/ports)
- **Application**: Casos de uso que implementan la lógica de negocio
- **Infrastructure**: Adaptadores que implementan los gateways del dominio
- **Bootstrap**: Configuración y arranque de la aplicación

### 2. **Reactive Programming**
- Uso de **Spring WebFlux** con programación reactiva
- Tipos reactivos: `Mono<T>` y `Flux<T>` de Project Reactor
- Base de datos reactiva con **R2DBC** (PostgreSQL)

### 3. **Functional Programming**
- Casos de uso implementados como `Function<Input, Mono<Output>>`
- Handlers funcionales para endpoints REST
- Router funcional en lugar de controllers tradicionales

## Estructura Detallada por Módulo

### Domain (Núcleo del Negocio)
```
domain/src/main/java/com/vitalitsoft/domain/
├── auth/                    # Entidad de autenticación
│   ├── AuthModel.java       # Modelo de usuario
│   ├── TokenModel.java      # Modelo de tokens
│   └── gateways/            # Interfaces (ports)
│       ├── AuthRepository.java
│       ├── JwtRepository.java
│       └── PasswordRepository.java
├── refreshtoken/            # Entidad de refresh tokens
├── userToken/              # Tokens de usuario (reset password, etc.)
├── otp/                    # One-Time Password
├── events/                 # Eventos del sistema
└── shared/                 # Elementos compartidos
    ├── enums/              # Enumeraciones
    ├── constants/          # Constantes
    └── exception/          # Excepciones de dominio
```

### Application (Casos de Uso)
```
application/src/main/java/com/vitalitsoft/application/
├── dto/                    # Data Transfer Objects
│   ├── auth/
│   ├── passwordReset/
│   └── otp/
├── usecase/               # Casos de uso
│   ├── auth/
│   ├── passwordReset/
│   └── otp/
└── mapper/               # Mappers entre DTOs y Models
```

### Infrastructure (Adaptadores)
```
infrastructure/src/main/java/com/vitalitsoft/infrastructure/
├── entry/points/api/              # Adaptadores de entrada
│   ├── auth/                      # Handlers y routers REST
│   ├── config/                    # Configuración de API
│   ├── filters/                   # Filtros HTTP
│   └── shared/                    # Utilidades compartidas
└── driven/adapters/              # Adaptadores de salida
    ├── r2dbc/                    # Adaptador de base de datos
    ├── security/                 # Adaptador de seguridad/JWT
    └── rabbit/                   # Adaptador de mensajería
```

### Bootstrap (Configuración y Arranque)
```
bootstrap/src/main/java/com/vitalitsoft/
├── MainApplication.java          # Punto de entrada principal
└── config/                       # Configuraciones globales
```

## Tecnologías y Frameworks

### Core Technologies
- **Java 17** - Lenguaje principal
- **Spring Boot 4.0.2** - Framework principal
- **Spring WebFlux** - Programación reactiva
- **Project Reactor** - Biblioteca reactiva
- **R2DBC** - Base de datos reactiva
- **PostgreSQL** - Base de datos

### Security & Authentication
- **JWT** - Tokens de autenticación
- **Spring Security** - Seguridad
- **BCrypt** - Encriptación de contraseñas

### Build & Deployment
- **Gradle** - Herramienta de build
- **Docker** - Contenerización
- **Docker Compose** - Orquestación local

### Testing & Quality
- **JUnit 5** - Testing framework
- **Mockito** - Mocking
- **AssertJ** - Assertions
- **Lombok** - Reducción de boilerplate

## Patrones de Diseño Implementados

### 1. **Repository Pattern**
```java
// Gateway (Interface en Domain)
public interface AuthRepository {
    Mono<AuthModel> findByEmail(String email);
    Mono<AuthModel> save(AuthModel auth);
}

// Implementación en Infrastructure
@Component
public class AuthRepositoryAdapter extends ReactiveAdapterOperations<AuthModel, AuthData, UUID, AuthDataRepository>
    implements AuthRepository {
    // Implementación específica
}
```

### 2. **Use Case Pattern**
```java
@RequiredArgsConstructor
public class LoginUseCase implements Function<LoginRequest, Mono<TokenModel>> {
    private final AuthRepository authRepository;
    private final JwtRepository jwtRepository;
    
    @Override
    public Mono<TokenModel> apply(LoginRequest request) {
        // Lógica del caso de uso
    }
}
```

### 3. **Handler Pattern (Functional Routing)**
```java
@Component
public class AuthHandler {
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
            .flatMap(loginUseCase)
            .flatMap(token -> ServerResponse.ok().bodyValue(token));
    }
}
```

### 4. **Adapter Pattern**
- `ReactiveAdapterOperations` - Clase base para adaptadores R2DBC
- Adaptadores específicos para cada entidad de dominio

## Convenciones de Código

### Naming Conventions
- **Models**: `*Model.java` (ej: AuthModel, TokenModel)
- **DTOs**: `*Request.java`, `*Response.java`
- **Use Cases**: `*UseCase.java`
- **Repositories**: `*Repository.java` (interfaces), `*RepositoryAdapter.java` (implementaciones)
- **Handlers**: `*Handler.java`
- **Routers**: `*RouterRest.java`

### Package Structure
- Agrupación por feature/dominio, no por tipo técnico
- Separación clara entre puertos (interfaces) y adaptadores (implementaciones)

### Reactive Patterns
- Uso consistente de `Mono<T>` para operaciones individuales
- Uso de `Flux<T>` para streams de datos
- Manejo de errores reactivo con `onErrorMap`, `doOnError`

## Configuración de Perfiles

### Perfiles Disponibles
- **dev** - Desarrollo local
- **prod** - Producción

### Variables de Entorno
```properties
# Base de datos
R2DBC_HOST=localhost
R2DBC_PORT=5432
R2DBC_DATABASE=postgres
R2DBC_USERNAME=postgres
R2DBC_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600000
JWT_HEADER=Authorization
JWT_PREFIX=Bearer 

# Servidor
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

## Funcionalidades del Sistema

### Autenticación
- Registro de usuarios
- Login con JWT
- Refresh tokens
- Logout

### Gestión de Contraseñas
- Solicitud de reset de contraseña
- Validación de tokens de reset
- Confirmación de nueva contraseña

### OTP (One-Time Password)
- Envío de códigos OTP
- Validación de códigos OTP

### Eventos
- Sistema de eventos con RabbitMQ
- Eventos de registro de usuario
- Eventos de reset de contraseña

## Instrucciones para GitHub Copilot

### Al trabajar con este proyecto:

1. **Mantén la arquitectura hexagonal**: Separa claramente responsabilidades entre capas
2. **Usa programación reactiva**: Siempre retorna `Mono<T>` o `Flux<T>`
3. **Implementa casos de uso como Functions**: `Function<Input, Mono<Output>>`
4. **Sigue las convenciones de naming** establecidas
5. **Usa Lombok** para reducir boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
6. **Maneja errores de forma reactiva** con excepciones personalizadas (`NexusException`)
7. **Implementa validación** usando Bean Validation y `ObjectValidator`
8. **Usa mappers** para conversión entre DTOs y Models
9. **Mantén la separación** entre puertos (interfaces) y adaptadores (implementaciones)
10. **Aplica principios SOLID** y Clean Code

### Patrones específicos a seguir:

#### Crear un nuevo caso de uso:
```java
@Slf4j
@RequiredArgsConstructor
public class NewFeatureUseCase implements Function<RequestDto, Mono<ResponseDto>> {
    private final DomainRepository repository;
    
    @Override
    public Mono<ResponseDto> apply(RequestDto request) {
        return validateRequest(request)
            .flatMap(this::processBusinessLogic)
            .map(this::mapToResponse)
            .doOnError(error -> log.error("Error in NewFeatureUseCase", error));
    }
}
```

#### Crear un nuevo adaptador de repositorio:
```java
@Component
public class NewRepositoryAdapter extends ReactiveAdapterOperations<DomainModel, DataEntity, UUID, DataRepository>
    implements DomainRepository {
    
    public NewRepositoryAdapter(DataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, DataEntity::toDomain);
    }
    
    // Implementar métodos específicos del dominio
}
```

#### Crear un nuevo handler:
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class NewFeatureHandler {
    private final NewFeatureUseCase useCase;
    private final ObjectValidator validator;
    
    public Mono<ServerResponse> handleRequest(ServerRequest request) {
        return request.bodyToMono(RequestDto.class)
            .doOnNext(validator::validate)
            .flatMap(useCase)
            .flatMap(response -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response))
            .onErrorResume(ExceptionUtils::handleError);
    }
}
```

Esta estructura garantiza consistencia, mantenibilidad y adherencia a los principios de Clean Architecture implementados en el proyecto.
