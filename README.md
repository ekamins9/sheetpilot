# SheetPilot

SheetPilot is a powerful spreadsheet automation and management platform built to simplify data processing, collaboration, and workflow automation for teams and individuals.

## Overview

SheetPilot provides a robust backend API for managing spreadsheet operations, data transformations, and automated workflows. The platform is designed to integrate seamlessly with modern frontend applications and provide scalable, reliable spreadsheet processing capabilities.

## Tech Stack

### Backend
- **Java 17+** - Modern Java runtime
- **Spring Boot 3.2.1** - Application framework
- **Spring Data JPA** - Data persistence layer
- **PostgreSQL** - Primary database
- **Flyway** - Database migration management
- **Maven** - Build and dependency management
- **Lombok** - Boilerplate code reduction

### Infrastructure
- **Railway** - Cloud deployment platform
- **PostgreSQL** - Managed database service

## Project Structure

```
sheetpilot/
├── src/
│   ├── main/
│   │   ├── java/com/sheetpilot/
│   │   │   ├── config/          # Application configuration
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── service/         # Business logic layer
│   │   │   ├── model/           # Domain models and entities
│   │   │   ├── repository/      # Data access layer
│   │   │   └── SheetPilotApplication.java
│   │   └── resources/
│   │       ├── application.yml  # Application configuration
│   │       └── db/migration/    # Flyway database migrations
│   └── test/
│       └── java/com/sheetpilot/ # Test files
├── pom.xml                       # Maven configuration
├── railway.json                  # Railway deployment config
└── README.md
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+ (or use Railway for managed database)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd sheetpilot
   ```

2. **Set up PostgreSQL database**
   ```bash
   createdb sheetpilot
   ```

3. **Configure environment variables** (optional)
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/sheetpilot
   export DATABASE_USERNAME=postgres
   export DATABASE_PASSWORD=postgres
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Building for Production

```bash
# Create production JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/sheetpilot-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Health Check
- `GET /api/health` - Application health status
- `GET /actuator/health` - Spring Actuator health endpoint

## Configuration

The application can be configured using environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/sheetpilot` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | `postgres` |
| `PORT` | Server port | `8080` |
| `LOG_LEVEL` | Application log level | `INFO` |
| `SHOW_SQL` | Show SQL queries in logs | `false` |

## Deployment

### Railway

The project is configured for deployment on Railway:

1. Connect your GitHub repository to Railway
2. Add a PostgreSQL database service
3. Railway will automatically:
   - Detect the `railway.json` configuration
   - Build using Maven
   - Run database migrations
   - Start the application

Required environment variables on Railway:
- `DATABASE_URL` - Automatically provided by Railway PostgreSQL service
- `DATABASE_USERNAME` - Automatically provided by Railway PostgreSQL service
- `DATABASE_PASSWORD` - Automatically provided by Railway PostgreSQL service

## CORS Configuration

The application is configured to allow requests from:
- `http://localhost:5173` (local development)
- `*.vercel.app` (production deployments)

## Database Migrations

Database schema is managed using Flyway migrations located in `src/main/resources/db/migration/`.

Migrations are automatically applied on application startup.

To create a new migration:
1. Create a new SQL file in `src/main/resources/db/migration/`
2. Name it following the pattern: `V{version}__{description}.sql`
   - Example: `V2__add_users_table.sql`

## Development Guidelines

- Follow standard Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write unit tests for all service layer methods
- Use DTOs for API request/response objects
- Keep controllers thin - business logic belongs in services
- Document all public API endpoints

## License

MIT License - see LICENSE file for details