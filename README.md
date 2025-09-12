# 🏋️ Gym Management Microservices System

A comprehensive **Spring Cloud** microservices architecture for managing gym operations. Built with **Spring Boot**, **Eureka Discovery**, **OpenFeign**, and **JWT Authentication**, this system provides scalable REST APIs for users, trainees, trainers, training management, and workload tracking.

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Services](#-services)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Service Configuration](#-service-configuration)
- [API Documentation](#-api-documentation)
- [Authentication & Security](#-authentication--security)
- [Service Communication](#-service-communication)
- [Monitoring & Health Checks](#-monitoring--health-checks)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Eureka Service Registry                   │
│                     (localhost:8761)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┼─────────┐
                    │         │         │
        ┌───────────▼──┐ ┌────▼────┐ ┌──▼────────────┐
        │ Main Service │ │ Trainer │ │ Other Services│
        │   (8080)     │ │Workload │ │   (8083+)     │
        │              │ │(8082)   │ │               │
        └──────────────┘ └─────────┘ └───────────────┘
                │                │
           ┌────▼────┐      ┌────▼────┐
           │   H2    │      │   In    │
           │Database │      │Memory DB│
           └─────────┘      └─────────┘
```

### Key Features
- **Microservices Architecture**: Loosely coupled, independently deployable services
- **Service Discovery**: Eureka Netflix for automatic service registration and discovery
- **Inter-Service Communication**: OpenFeign with load balancing and circuit breaker patterns
- **Centralized Authentication**: JWT-based security with service-to-service authentication
- **API Gateway Ready**: Architecture prepared for API Gateway integration
- **Fault Tolerance**: Circuit breaker pattern with fallback mechanisms
- **Scalability**: Each service can be scaled independently

## 🔧 Services

| Service | Port | Purpose | Database      |
|---------|------|---------|---------------|
| **Eureka Registry** | 8761 | Service discovery and registration | N/A           |
| **Main Service** | 8080 | Core gym operations (users, trainers, trainees, trainings) | H2/PostgreSQL |
| **Trainer Workload Service** | 8082 | Trainer workload calculation and tracking | In Memory DB |

### Main Service
- User management (authentication, profiles)
- Trainer management (specializations, availability)
- Trainee management (registrations, profiles)
- Training management (sessions, scheduling)
- Training types management
- JWT token generation and validation

### Trainer Workload Service
- Real-time workload calculation
- Training duration tracking
- Trainer capacity management
- Workload analytics and reporting

## 📚 Tech Stack

### Core Technologies
| Technology | Purpose |
|------------|---------|
| **Java 17+** | Programming Language |
| **Spring Boot 3.x** | Microservice Framework |
| **Spring Cloud** | Microservices Infrastructure |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Data Access Layer |

### Microservices Infrastructure
| Technology | Purpose |
|------------|---------|
| **Eureka Netflix** | Service Discovery |
| **OpenFeign** | Declarative REST Client |
| **Spring Cloud LoadBalancer** | Client-side Load Balancing |
| **Circuit Breaker** | Fault Tolerance |
| **JWT** | Stateless Authentication |

### Data & Documentation
| Technology | Purpose |
|------------|---------|
| **H2 Database** | Development & Testing |
| **PostgreSQL** | Production Database |
| **Swagger/OpenAPI 3** | API Documentation |
| **Spring Boot Actuator** | Health Monitoring |

### Testing & Build
| Technology | Purpose |
|------------|---------|
| **JUnit 5 & Mockito** | Testing Framework |
| **JaCoCo** | Code Coverage |
| **Maven** | Build & Dependency Management |

## 🛠 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Git**
- **Docker & Docker Compose** (for PostgreSQL)
- **IDE** (IntelliJ IDEA recommended)

## 🚀 Quick Start

### 1. Clone and Build
```bash
git clone https://github.com/felipemunevarn/gymRest.git
cd gymRest

# Build all services
mvn clean install
```

### 2. Start Services (Recommended Order)

**Option A: Using Maven**
```bash
# Terminal 1: Start Eureka Registry
cd eureka-server
mvn spring-boot:run

# Terminal 2: Start Main Service (wait for Eureka to be up)
cd main-service
mvn spring-boot:run

# Terminal 3: Start Trainer Workload Service
cd trainer-workload-service
mvn spring-boot:run
```

**Option B: Using JAR files**
```bash
# Build all services first
mvn clean package

# Start services
java -jar eureka-server/target/eureka-server.jar
java -jar main-service/target/main-service.jar
java -jar trainer-workload-service/target/trainer-workload-service.jar
```

### 3. Verify Deployment
- **Eureka Dashboard**: http://localhost:8761
- **Main Service Health**: http://localhost:8080/actuator/health
- **Workload Service Health**: http://localhost:8082/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html

## ⚙️ Service Configuration

### Environment Profiles

Each service supports multiple Spring profiles:

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | H2 (in-memory) | Development |
| `test` | H2 (in-memory) | Testing |
| `local` | PostgreSQL | Local development |
| `deploy` | PostgreSQL | Production |

### Eureka Configuration

**application.yml** (each service):
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

spring:
  application:
    name: main-service  # or trainer-workload-service
```

### JWT Configuration

Both services must share the same JWT secret for authentication:

**Main Service**:
```yaml
jwt:
  secret: 870fc857a079157a69c5c03a8788a0c4721d90f8fe35476d1bce3609fc2ede4f
  expiration: 86400000
```

**Trainer Workload Service**:
```yaml
jwt:
  secret: 870fc857a079157a69c5c03a8788a0c4721d90f8fe35476d1bce3609fc2ede4f
  expiration: 86400000
```

### Database Setup

**PostgreSQL with Docker Compose**:
```bash
docker-compose up -d  # Start PostgreSQL
docker-compose down   # Stop and remove
```

## 📖 API Documentation

### Swagger UI Endpoints
- **Main Service**: http://localhost:8080/swagger-ui.html
- **Trainer Workload Service**: http://localhost:8081/swagger-ui.html

### Main Service Endpoints

| Endpoint                            | Description |
|-------------------------------------|-------------|
| `POST /api/v1/auth/login`           | User authentication |
| `GET/POST/PUT/DELETE /api/v1/users` | User management |
| `GET/POST/PUT /api/v1/trainers`     | Trainer management |
| `GET/POST/PUT /api/v1/trainees`     | Trainee management |
| `POST /api/v1/trainings`            | Create training sessions |
| `GET /api/v1/training-types`        | Training type management |

### Trainer Workload Service Endpoints

| Endpoint                                   | Description |
|--------------------------------------------|-------------|
| `POST /api/v1/workload/add`                | Add training to workload |
| `GET /api/v1/workload/trainer/{trainerId}` | Get trainer workload |
| `PUT /api/v1/workload/update`              | Update trainer workload |

## 🔐 Authentication & Security

### JWT Token Structure
```json
{
  "sub": "username",
  "service": true,        // for service-to-service calls
  "serviceName": "main-service",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### Service-to-Service Authentication
```java
// Automatic JWT injection via Feign interceptor
@FeignClient(name = "trainer-workload-service")
public interface WorkloadServiceClient {
    @PostMapping("/api/v1/workload/add")
    ResponseEntity<String> addWorkload(@RequestBody WorkloadRequest request);
}
```

### Security Configuration
- **Public endpoints**: `/actuator/health`, `/swagger-ui/**`
- **Protected endpoints**: All `/api/**` routes
- **Service endpoints**: Require `ROLE_SERVICE` or `ROLE_USER`

## 🔄 Service Communication

### OpenFeign Configuration

**Main Service → Trainer Workload Service**:
```java
@FeignClient(
    name = "trainer-workload-service",
    fallback = WorkloadServiceFallback.class
)
public interface WorkloadServiceClient {
    @PostMapping("/api/v1/workload/update")
    ResponseEntity<Void> updateTrainerWorkload(
        @RequestBody TrainerWorkloadRequest request
    );
}
```

### Circuit Breaker & Fallback
```java
@Component
public class WorkloadServiceFallback implements WorkloadServiceClient {
    @Override
    public ResponseEntity<Void> updateTrainerWorkload(TrainerWorkloadRequest request) {
        // Fallback logic: queue for retry, send notification, etc.
        log.error("Workload service unavailable - implementing fallback");
        return ResponseEntity.ok().build();
    }
}
```

### Load Balancing
Automatic client-side load balancing when multiple service instances are running:
```bash
# Start multiple instances of a service
java -jar trainer-workload-service.jar --server.port=8081
java -jar trainer-workload-service.jar --server.port=8082
```

## 🏥 Monitoring & Health Checks

### Actuator Endpoints

Each service exposes monitoring endpoints:

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Service health status |
| `/actuator/info` | Service information |
| `/actuator/metrics` | Performance metrics |
| `/actuator/eureka` | Eureka registration info |

### Service Discovery Health
Monitor all registered services via Eureka dashboard:
```
http://localhost:8761
```

### Health Check URLs
- **Overall System Health**: Check all services are registered in Eureka
- **Individual Service Health**:
    - http://localhost:8080/actuator/health
    - http://localhost:8082/actuator/health

## ✅ Testing

### Running Tests

**All Services**:
```bash
mvn clean test
```

**Specific Service**:
```bash
cd main-service
mvn test
```

**Integration Testing**:
```bash
# Start all services first, then run integration tests
mvn test -Dtest=*IntegrationTest
```

### Code Coverage
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Contract Testing
Consider implementing contract testing between services using Spring Cloud Contract or Pact.

## 🚢 Deployment

### Docker Deployment

**Build Docker Images**:
```bash
# Each service
docker build -t gym-eureka-server .
docker build -t gym-main-service .
docker build -t gym-workload-service .
```

**Docker Compose**:
```yaml
version: '3.8'
services:
  eureka-server:
    image: gym-eureka-server
    ports:
      - "8761:8761"
  
  main-service:
    image: gym-main-service
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
      - postgres
  
  trainer-workload-service:
    image: gym-workload-service
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - postgres
  
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: gym_db
      POSTGRES_USER: gym_user
      POSTGRES_PASSWORD: gym_password
```

### Production Considerations

1. **API Gateway**: Add Spring Cloud Gateway for unified entry point
2. **Config Server**: Externalize configuration with Spring Cloud Config
3. **Distributed Tracing**: Implement with Sleuth/Zipkin
4. **Centralized Logging**: ELK Stack or similar
5. **Service Mesh**: Consider Istio for advanced traffic management
6. **Database Per Service**: Implement separate databases for each service

### Scaling
```bash
# Scale services independently
docker-compose up --scale main-service=2 --scale trainer-workload-service=3
```

## 🔮 Future Enhancements

- **API Gateway**: Spring Cloud Gateway integration
- **Configuration Server**: Centralized configuration management
- **Message Queues**: RabbitMQ/Kafka for async processing
- **Caching**: Redis for improved performance
- **Distributed Tracing**: Request tracing across services
- **Monitoring**: Prometheus + Grafana dashboards
- **Security**: OAuth2 + Spring Cloud Security

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/microservice-enhancement`)
3. Implement your changes following microservices best practices
4. Add tests for new functionality
5. Update documentation
6. Submit a Pull Request

### Development Guidelines
- Each service should be independently deployable
- Maintain backward compatibility in APIs
- Implement proper circuit breaker patterns
- Follow 12-factor app principles
- Add comprehensive testing (unit, integration, contract)

## 📞 Support

For issues, questions, or contributions:

1. Check [Issues](https://github.com/felipemunevarn/gymRest/issues)
2. Create detailed issue reports
3. Join discussions on architecture decisions

---

**Built with ❤️ using Spring Cloud Microservices** 🚀

*This microservices architecture provides scalability, fault tolerance, and maintainability for enterprise-grade gym management systems.*