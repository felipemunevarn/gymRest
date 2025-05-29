# 🏋️ Gym Management System

A comprehensive **Spring Boot** application for managing gym operations with REST APIs for users, trainees, trainers, and training types. Built with **Spring Data JPA (Hibernate)** for ORM and includes comprehensive API documentation and monitoring capabilities.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Monitoring](#-monitoring)
- [Contributing](#-contributing)

## ✨ Features

- **User Management**: Complete CRUD operations for gym users
- **Trainer Management**: Manage trainer profiles and specializations
- **Trainee Management**: Handle trainee registrations and profiles
- **Training Types**: Categorize and manage different training programs
- **RESTful APIs**: Well-structured REST endpoints
- **Interactive Documentation**: Swagger/OpenAPI 3 integration
- **Health Monitoring**: Spring Boot Actuator endpoints
- **Test Coverage**: JaCoCo integration for code coverage analysis
- **Multi-Environment Support**: Different profiles for development, testing, and production

## 🔧 Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 17+** | Programming Language |
| **Spring Boot** | Application Framework |
| **Spring Data JPA (Hibernate)** | ORM & Data Access |
| **H2 Database** | In-memory database (dev/test) |
| **PostgreSQL** | Production database |
| **Swagger/OpenAPI 3** | API Documentation |
| **JaCoCo** | Code Coverage Analysis |
| **JUnit & Mockito** | Testing Framework |
| **Maven** | Build Tool |
| **Spring Boot Actuator** | Application Monitoring |

## 🛠 Prerequisites

Before running this application, ensure you have:

- **Java 17** or higher installed
- **Maven 3.6+** for dependency management
- **Git** for version control
- **Docker & Docker Compose** (for PostgreSQL setup)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/felipemunevarn/gymRest.git
   cd gymRest
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

## ⚙️ Configuration

### Environment Profiles

The application supports multiple Spring profiles for different environments:

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | H2 (in-memory) | Development |
| `test` | H2 (in-memory) | Running tests |
| `local` | PostgreSQL | Local development with persistent data |
| `deploy` | PostgreSQL | Production deployment |

### Database Setup

#### Option 1: H2 Database (Default for dev/test)
No additional setup required. The application will use an in-memory H2 database.

#### Option 2: PostgreSQL (for local/deploy profiles)

1. **Start PostgreSQL with Docker Compose**
   ```bash
   # Start PostgreSQL container
   docker-compose up -d
   
   # Stop and remove container (when done)
   docker-compose down -v
   ```

2. **Manual PostgreSQL Setup** (alternative)
    - Install PostgreSQL locally
    - Create a database named `gym_db` (or as configured)
    - Update `application-local.properties` with your database credentials

### Profile Activation

Choose one of the following methods to activate a profile:

**Method 1: Maven Command**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Method 2: Environment Variable**
```bash
# Unix/Linux/macOS
export SPRING_PROFILES_ACTIVE=local

# Windows
set SPRING_PROFILES_ACTIVE=local
```

**Method 3: IDE Configuration**
Set the active profile in your IDE's run configuration (e.g., IntelliJ IDEA → Run Configuration → Active profiles)

**Method 4: Application Properties**
```properties
spring.profiles.active=local
```

## 🚀 Running the Application

1. **Start the application**
   ```bash
   # Using Maven (with profile)
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Or run the JAR file
   java -jar target/gym-management-system.jar
   ```

2. **Verify the application is running**
    - Open your browser and go to: `http://localhost:8080`
    - Check health status: `http://localhost:8080/actuator/health`

## 📖 API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:

**URL**: `http://localhost:8080/swagger-ui.html`

### Using the API Documentation

1. **Explore Endpoints**: Browse all available REST endpoints organized by controller
2. **View Request/Response Schemas**: Check the "Schemas" section at the bottom
3. **Test APIs Interactively**:
    - Click on any endpoint to expand details
    - Click "Try it out"
    - Fill in required parameters
    - Click "Execute" to test the API
    - View the response, including status codes and data

### Main API Endpoints

| Endpoint | Description |
|----------|-------------|
| `/api/users` | User management operations |
| `/api/trainers` | Trainer management operations |
| `/api/trainees` | Trainee management operations |
| `/api/training-types` | Training type management |

## ✅ Testing

### Running Tests

**Run all tests**
```bash
mvn clean test
```

**Run specific test class**
```bash
mvn test -Dtest=YourTestClassName
```

### Code Coverage with JaCoCo

1. **Generate coverage report**
   ```bash
   mvn clean test jacoco:report
   ```

2. **View coverage report**
   ```bash
   # Open the HTML report
   open target/site/jacoco/index.html        # macOS
   xdg-open target/site/jacoco/index.html    # Linux
   start target\site\jacoco\index.html       # Windows
   ```

The report shows:
- Line coverage percentage
- Branch coverage
- Method coverage
- Class coverage details

## 🩺 Monitoring

### Spring Boot Actuator Endpoints

The application includes several monitoring endpoints:

| Endpoint | Description | URL |
|----------|-------------|-----|
| Health | Application health status | `/actuator/health` |
| Info | Application information | `/actuator/info` |
| Metrics | Application metrics | `/actuator/metrics` |
| Loggers | View/modify log levels | `/actuator/loggers` |
| Mappings | All request mappings | `/actuator/mappings` |
| Environment | Environment variables | `/actuator/env` |

### Configuration

To expose all actuator endpoints (development only):
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

**⚠️ Security Note**: In production, limit exposed endpoints and secure them appropriately.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/felipemunevarn/gymRest/issues) page
2. Create a new issue with detailed information
3. Contact the maintainer

---

**Happy Coding!** 🚀