# SheetPilot

SheetPilot is a powerful spreadsheet automation and management platform built to simplify data processing, collaboration, and workflow automation for teams and individuals.

## Overview

SheetPilot provides a robust backend API for managing spreadsheet operations, data transformations, and automated workflows. The platform is designed to integrate seamlessly with modern frontend applications and provide scalable, reliable spreadsheet processing capabilities.

## Tech Stack

### Backend
- **Java 21** - Modern Java runtime (LTS)
- **Spring Boot 3.2.1** - Application framework
- **Spring Data JPA** - Data persistence layer
- **PostgreSQL** - Primary database
- **Flyway** - Database migration management
- **Maven** - Build and dependency management
- **Lombok** - Boilerplate code reduction

### Frontend
- **SvelteKit** - Modern web framework
- **TypeScript** - Type-safe JavaScript
- **Tailwind CSS v3** - Utility-first CSS framework
- **Vite** - Fast build tool and dev server

### Infrastructure
- **Railway** - Backend deployment platform
- **Vercel** - Frontend deployment platform
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
│   │       ├── application.yml      # Base configuration
│   │       ├── application-prod.yml # Production settings
│   │       └── db/migration/        # Flyway database migrations
│   └── test/
│       └── java/com/sheetpilot/     # Test files
├── frontend/
│   ├── src/
│   │   ├── lib/
│   │   │   ├── api/             # API client utilities
│   │   │   └── components/      # Svelte components
│   │   ├── routes/              # SvelteKit routes
│   │   ├── app.css              # Global styles
│   │   ├── app.html             # HTML template
│   │   └── app.d.ts             # TypeScript declarations
│   ├── static/                  # Static assets
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── vercel.json              # Vercel deployment config
├── pom.xml                      # Maven configuration
├── railway.json                 # Railway deployment config
├── Procfile                     # Process configuration
└── README.md
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Node.js 18+ and npm
- PostgreSQL 14+ (or use Railway for managed database)

### Local Development Setup

#### Backend

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

   The backend will start on `http://localhost:8080`

#### Frontend

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env to set PUBLIC_API_URL if needed (defaults to http://localhost:8080)
   ```

4. **Run the development server**
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173`

### Running Tests

#### Backend Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

#### Frontend Tests
```bash
cd frontend

# Type checking
npm run check

# Type checking (watch mode)
npm run check:watch
```

### Building for Production

#### Backend
```bash
# Create production JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/sheetpilot-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend

# Build for production
npm run build

# Preview production build
npm run preview
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

### Backend Deployment (Railway)

The backend is configured for deployment on Railway with PostgreSQL:

#### Initial Setup

1. **Create a Railway account** at [railway.app](https://railway.app)

2. **Create a new project**
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Connect your GitHub account and select the repository
   - Choose the root directory (contains `pom.xml`)

3. **Add PostgreSQL database**
   - In your project, click "New"
   - Select "Database" → "PostgreSQL"
   - Railway will automatically provision a PostgreSQL instance

4. **Configure environment variables**
   Railway automatically injects the following from the PostgreSQL service:
   - `DATABASE_URL` - PostgreSQL connection URL
   - `DATABASE_USERNAME` - Database username
   - `DATABASE_PASSWORD` - Database password

   Additional variables to set:
   - `SPRING_PROFILES_ACTIVE=prod` - Activate production profile
   - `PORT` - Automatically provided by Railway

5. **Deploy**
   - Railway will automatically:
     - Detect `railway.json` configuration
     - Build using `mvn clean package -DskipTests`
     - Run Flyway migrations
     - Start the application with `java -Dserver.port=$PORT -jar target/sheetpilot-0.0.1-SNAPSHOT.jar`
   - Health checks run at `/actuator/health`

6. **Get your backend URL**
   - After deployment, Railway provides a public URL
   - Example: `https://sheetpilot-production.railway.app`
   - Test health endpoint: `https://your-app.railway.app/api/health`

#### Configuration Files

- `railway.json` - Railway deployment configuration
- `Procfile` - Alternative process definition
- `src/main/resources/application-prod.yml` - Production-specific settings

### Frontend Deployment (Vercel)

The frontend is configured for deployment on Vercel:

#### Initial Setup

1. **Create a Vercel account** at [vercel.com](https://vercel.com)

2. **Import your project**
   - Click "New Project"
   - Import your GitHub repository
   - Set root directory to `frontend`

3. **Configure build settings**
   - Framework Preset: **SvelteKit**
   - Build Command: `npm run build`
   - Output Directory: `build` (auto-detected)
   - Install Command: `npm install`

4. **Set environment variables**
   - Go to Project Settings → Environment Variables
   - Add `PUBLIC_API_URL` with your Railway backend URL
   - Example: `https://sheetpilot-production.railway.app`

5. **Deploy**
   - Click "Deploy"
   - Vercel will automatically build and deploy your frontend
   - You'll get a URL like `https://your-project.vercel.app`

#### Automatic Deployments

Both Railway and Vercel support automatic deployments:

- **Production**: Deployments from `main` branch
- **Preview**: Deployments from pull requests
- **Rollbacks**: Easy one-click rollbacks in both platforms

#### Environment Variables Summary

**Railway (Backend)**:
```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<auto-provided>
DATABASE_USERNAME=<auto-provided>
DATABASE_PASSWORD=<auto-provided>
PORT=<auto-provided>
```

**Vercel (Frontend)**:
```bash
PUBLIC_API_URL=https://your-backend.railway.app
```

#### Post-Deployment Checklist

- [ ] Verify backend health: `https://your-backend.railway.app/api/health`
- [ ] Check database migrations ran successfully
- [ ] Test frontend can connect to backend
- [ ] Verify CORS settings allow your Vercel domain
- [ ] Check logs in Railway and Vercel dashboards
- [ ] Test critical user flows

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
