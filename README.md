# 🏋️ Gym Management Microservices System

A comprehensive **async messaging-based** microservices architecture for managing gym operations. Built with **Spring Boot**, **ActiveMQ**, and **JWT Authentication**, this system provides scalable REST APIs for users, trainees, trainers, training management, and workload tracking with resilient asynchronous communication.

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Services](#-services)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Service Configuration](#-service-configuration)
- [API Documentation](#-api-documentation)
- [Authentication & Security](#-authentication--security)
- [Async Messaging](#-async-messaging)
- [Monitoring & Health Checks](#-monitoring--health-checks)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## 🗺️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    ActiveMQ Message Broker                      │
│                     (localhost:61616)                           │
│               Web Console: localhost:8161                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Async Messages
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼───────┐     ┌───────▼───────┐     ┌─────▼─────┐
│ Main Service  │────▶│ Message Queue│◀────│  Trainer  │
│    (8080)     │     │ trainer.workload    │  Workload │
│               │     │   .updates    │     │  (8082)   │
└───────────────┘     └───────────────┘     └───────────┘
        │                                           │
   ┌────▼────┐                                 ┌────▼────┐
   │PostgreSQL│                                │In-Memory│
   │Database │                                 │   DB    │
   └─────────┘                                 └─────────┘
```

### Key Features
- **Async Microservices Architecture**: Event-driven, loosely coupled services
- **Message-Driven Communication**: ActiveMQ for reliable async messaging
- **Event Sourcing Ready**: Structured event messages for audit trails
- **Fault Tolerance**: Message persistence and Dead Letter Queue support
- **JWT Security**: End-to-end authentication including message validation
- **Scalability**: Services scale independently with message queue buffering
- **Resilience**: Services can be offline while messages queue safely

## 🔧 Services

| Service | Port | Purpose | Database | Communication |
|---------|------|---------|----------|---------------|
| **ActiveMQ Broker** | 61616 | Message broker for async communication | N/A | Message Queue |
| **ActiveMQ Console** | 8161 | Web UI for monitoring queues | N/A | HTTP |
| **Main Service** | 8080 | Core gym operations (users, trainers, trainees, trainings) | PostgreSQL | REST API + Message Producer |
| **Trainer Workload Service** | 8082 | Trainer workload calculation and tracking | In-Memory DB | REST API + Message Consumer |

### Main Service
- User management (authentication, profiles)
- Trainer management (specializations, availability)
- Trainee management (registrations, profiles)
- Training management (sessions, scheduling)
- Training types management
- JWT token generation and validation
- **Message Publisher**: Sends workload update events

### Trainer Workload Service
- Real-time workload calculation
- Training duration tracking
- Trainer capacity management
- Workload analytics and reporting
- **Message Consumer**: Processes workload update events

### ActiveMQ Broker
- **Persistent message storage**
- **Dead Letter Queue** for failed messages
- **High availability** message delivery
- **Web console** for monitoring and management

## 📚 Tech Stack

### Core Technologies
| Technology | Purpose |
|------------|---------|
| **Java 17+** | Programming Language |
| **Spring Boot 3.x** | Microservice Framework |
| **Spring JMS** | Java Message Service Integration |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Data Access Layer |

### Async Messaging Infrastructure
| Technology | Purpose |
|------------|---------|
| **Apache ActiveMQ Artemis** | Message Broker |
| **Spring JMS** | Message Producer/Consumer |
| **Jackson JSR310** | JSON Date/Time Serialization |
| **JWT** | Message & API Authentication |
| **Dead Letter Queue** | Failed Message Handling |

### Data & Documentation
| Technology | Purpose |
|------------|---------|
| **PostgreSQL** | Main Service Database |
| **H2 Database** | In-Memory DB for Workload Service |
| **Swagger/OpenAPI 3** | API Documentation |
| **Spring Boot Actuator** | Health Monitoring |

### Testing & Build
| Technology | Purpose |
|------------|---------|
| **JUnit 5 & Mockito** | Testing Framework |
| **JaCoCo** | Code Coverage |
| **Maven** | Build & Dependency Management |
| **Docker Compose** | Infrastructure Setup |

## 🛠 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker & Docker Compose** (for ActiveMQ & PostgreSQL)
- **Git**
- **IDE** (IntelliJ IDEA recommended)

## 🚀 Quick Start

### 1. Clone and Build
```bash
git clone https://github.com/felipemunevarn/gymRest.git
cd gymRest

# Build all services
mvn clean install
```

### 2. Start Infrastructure
```bash
# Start ActiveMQ and PostgreSQL
docker-compose up -d

# Verify ActiveMQ is running
# Web Console: http://localhost:8161/console (admin/admin)
```

### 3. Start Services

**Using Maven**
```bash
# Terminal 1: Start Main Service
cd main-service
mvn spring-boot:run

# Terminal 2: Start Trainer Workload Service
cd trainer-workload-service
mvn spring-boot:run
```

**Using JAR files**
```bash
# Build all services first
mvn clean package

# Start services
java -jar main-service/target/main-service.jar
java -jar trainer-workload-service/target/trainer-workload-service.jar
```

### 4. Verify Deployment
- **ActiveMQ Console**: http://localhost:8161/console
- **Main Service Health**: http://localhost:8080/actuator/health
- **Workload Service Health**: http://localhost:8082/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Message Queue**: Check `trainer.workload.updates` in ActiveMQ console

## ⚙️ Service Configuration

### Docker Compose Infrastructure

```yaml
services:
  postgres:
    image: postgres:latest
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: jpa_epam
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres

  activemq:
    image: apache/activemq-artemis:latest-alpine
    ports:
      - "61616:61616"    # Message broker
      - "8161:8161"      # Web console
    environment:
      ARTEMIS_USER: admin
      ARTEMIS_PASSWORD: admin
```

### ActiveMQ Configuration

**Main Service (application.properties)**:
```properties
# ActiveMQ Configuration
spring.artemis.broker-url=tcp://localhost:61616
spring.artemis.user=admin
spring.artemis.password=admin
spring.jms.template.delivery-mode=persistent

# Queue Configuration
app.queue.trainer-workload=trainer.workload.updates
```

**Trainer Workload Service (application.yml)**:
```yaml
spring:
  artemis:
    broker-url: tcp://localhost:61616
    user: admin
    password: admin
  jms:
    template:
      delivery-mode: persistent

app:
  queue:
    trainer-workload: trainer.workload.updates
```

### JWT Configuration

Both services share the same JWT secret for message authentication:

**Main Service**:
```properties
jwt.secret=870fc857a079157a69c5c03a8788a0c4721d90f8fe35476d1bce3609fc2ede4f
jwt.expiration=86400000
```

**Trainer Workload Service**:
```yaml
jwt:
  secret: 870fc857a079157a69c5c03a8788a0c4721d90f8fe35476d1bce3609fc2ede4f
  expiration: 86400000
```

## 📖 API Documentation

### Swagger UI Endpoints
- **Main Service**: http://localhost:8080/swagger-ui.html
- **Trainer Workload Service**: http://localhost:8082/swagger-ui.html

### Main Service REST Endpoints

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/api/v1/auth/login` | POST | User authentication |
| `/api/v1/users` | GET/POST/PUT/DELETE | User management |
| `/api/v1/trainers` | GET/POST/PUT | Trainer management |
| `/api/v1/trainees` | GET/POST/PUT | Trainee management |
| `/api/v1/trainings` | POST/DELETE | Training sessions (triggers async workload updates) |
| `/api/v1/training-types` | GET | Training type management |

### Trainer Workload Service REST Endpoints

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/api/v1/trainers/workload` | POST | Direct workload update (legacy) |
| `/api/v1/trainers/{username}/workload` | GET | Get trainer workload |
| `/api/v1/trainers/{username}/workload/{year}/{month}` | GET | Monthly workload |
| `/api/v1/trainers` | GET | Get all trainers |
| `/actuator/health` | GET | Service health check |

## 🔐 Authentication & Security

### JWT Token in Messages

Each async message includes JWT authentication:
```json
{
  "messageId": "uuid",
  "messageType": "TRAINER_WORKLOAD_UPDATE",
  "timestamp": "2025-09-25T10:30:00",
  "source": "main-service",
  "authToken": "eyJhbGciOiJIUzI1NiJ9...",
  "payload": {
    "trainerUsername": "john.doe",
    "actionType": "ADD",
    "trainingDuration": 60
  }
}
```

### Security Layers
1. **REST API Security**: JWT tokens for HTTP requests
2. **Message Security**: JWT validation in async messages
3. **Service Authentication**: Each service validates message sender
4. **Message Integrity**: Structured message validation

### Authentication Flow
```
User Request → JWT Token → Main Service → Async Message (with JWT) → Workload Service
```

## 📨 Async Messaging

### Message Flow Architecture

```
Training Created/Deleted → Message Producer → ActiveMQ Queue → Message Consumer → Workload Update
```

### Message Types

**Trainer Workload Update Event**:
```json
{
  "messageId": "27cae371-28a9-45f8-8675-a815ea65bb59",
  "messageType": "TRAINER_WORKLOAD_UPDATE",
  "timestamp": "2025-09-25T22:13:54",
  "source": "main-service",
  "authToken": "eyJhbGciOiJIUzI1NiJ9...",
  "payload": {
    "trainerUsername": "maria.ramirez",
    "trainerFirstName": "Maria",
    "trainerLastName": "Ramirez",
    "isActive": true,
    "trainingDate": "2024-09-24",
    "trainingDuration": 110,
    "actionType": "ADD"
  }
}
```

### Queue Configuration

| Queue Name | Purpose | Consumers | Persistence |
|------------|---------|-----------|-------------|
| `trainer.workload.updates` | Workload updates | Trainer Workload Service | Yes |
| `DLQ.trainer.workload.updates` | Failed messages | Manual/Monitoring | Yes |

### Dead Letter Queue (DLQ)

Handles messages that fail processing:
- **Invalid message format**
- **Authentication failures**
- **Service processing errors**
- **Automatic retry with exponential backoff**
- **Manual recovery options**

### Benefits of Async Architecture

1. **Resilience**: Services can be offline while messages queue
2. **Scalability**: Handle traffic spikes with message buffering
3. **Decoupling**: Services evolve independently
4. **Reliability**: Message persistence ensures no data loss
5. **Monitoring**: Track message flow and processing rates

## 🏥 Monitoring & Health Checks

### ActiveMQ Monitoring

**Web Console**: http://localhost:8161/console
- Queue depths and message rates
- Consumer connections
- Dead letter queue monitoring
- Broker statistics

### Service Health Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Service health status |
| `/actuator/info` | Service information |
| `/actuator/metrics` | Performance metrics |
| `/actuator/jms` | JMS connection status |

### Key Metrics to Monitor

- **Message Queue Depth**: Messages waiting to be processed
- **Consumer Lag**: Time between message send and processing
- **Dead Letter Queue**: Failed messages requiring attention
- **Processing Rate**: Messages processed per second
- **Error Rate**: Failed vs successful message processing

## ✅ Testing

### Testing Async Communication

**Message Flow Testing**:
```bash
# 1. Create training (triggers message)
curl -X POST localhost:8080/api/v1/trainings \
  -H "Authorization: Bearer <token>" \
  -d '{"trainerUsername": "john.doe", ...}'

# 2. Check ActiveMQ console for message
# 3. Verify workload service processed message
curl localhost:8082/api/v1/trainers/john.doe/workload
```

### Resilience Testing

**Service Offline Test**:
```bash
# 1. Stop trainer-workload service
# 2. Create training (message queues)
# 3. Start trainer-workload service
# 4. Verify queued message is processed
```

**Running Tests**:
```bash
# All services
mvn clean test

# Integration tests
mvn test -Dtest=*IntegrationTest

# Message testing
mvn test -Dtest=*MessageTest
```

## 🚢 Deployment

### Docker Deployment

**Complete Stack**:
```yaml
version: '3.8'
services:
  activemq:
    image: apache/activemq-artemis:latest-alpine
    ports:
      - "61616:61616"
      - "8161:8161"
    environment:
      ARTEMIS_USER: admin
      ARTEMIS_PASSWORD: admin

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: gym_db
      POSTGRES_USER: gym_user
      POSTGRES_PASSWORD: gym_password

  main-service:
    image: gym-main-service
    ports:
      - "8080:8080"
    depends_on:
      - activemq
      - postgres
    environment:
      SPRING_ARTEMIS_BROKER_URL: tcp://activemq:61616

  trainer-workload-service:
    image: gym-workload-service
    ports:
      - "8082:8082"
    depends_on:
      - activemq
    environment:
      SPRING_ARTEMIS_BROKER_URL: tcp://activemq:61616
```

### Production Considerations

1. **Message Broker Clustering**: ActiveMQ HA setup
2. **Database Per Service**: Separate databases for data isolation
3. **Message Monitoring**: Prometheus + Grafana for message metrics
4. **Log Aggregation**: Centralized logging for message tracing
5. **Circuit Breakers**: Additional resilience patterns
6. **Message Versioning**: Schema evolution for message compatibility

### Scaling

**Horizontal Scaling**:
```bash
# Scale message consumers
docker-compose up --scale trainer-workload-service=3

# Multiple queue consumers automatically load-balance
```

## 🔮 Future Enhancements

- **API Gateway**: Single entry point for REST APIs
- **Event Sourcing**: Full event history with replay capabilities
- **CQRS Pattern**: Separate read/write data models
- **Message Schemas**: Avro or JSON Schema for message validation
- **Distributed Tracing**: Message correlation across services
- **Stream Processing**: Real-time analytics with Kafka Streams
- **Saga Pattern**: Distributed transaction management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/async-enhancement`)
3. Implement changes following async messaging patterns
4. Add message testing
5. Update documentation
6. Submit Pull Request

### Development Guidelines
- Design for async-first communication
- Implement idempotent message processors
- Add proper message validation and error handling
- Follow event-driven architecture principles
- Test message flows and failure scenarios

## 📞 Support

For issues, questions, or contributions:

1. Check [Issues](https://github.com/felipemunevarn/gymRest/issues)
2. Create detailed issue reports with message traces
3. Join discussions on async architecture patterns

---

**Built with ❤️ using Async Microservices + ActiveMQ** 🚀

*This event-driven architecture provides superior scalability, resilience, and maintainability for enterprise gym management systems.*