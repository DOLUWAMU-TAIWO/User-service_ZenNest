# QoreLabs User Service

![QoreLabs Logo](https://qorelabs.space/logo.png)

## 📋 Overview

QoreLabs User Service is a robust, scalable microservice designed to handle user management, authentication, and authorization for QoreLabs applications. It provides a comprehensive solution for user registration, authentication (including OAuth2), profile management, and role-based access control.

## ✨ Features

- **User Management**
  - Registration and account creation
  - Profile management
  - Role-based access control (TENANT, LANDLORD, USER, ADMIN)
  - User search and filtering

- **Authentication & Security**
  - JWT-based authentication
  - OAuth2 integration (Google, GitHub)
  - Password encryption with BCrypt
  - API key authentication
  - Email verification

- **API Options**
  - RESTful endpoints
  - GraphQL API with GraphiQL interface
  - Comprehensive query and mutation operations

- **Infrastructure**
  - Containerized with Docker
  - Stateless architecture
  - Redis caching
  - PostgreSQL database
  - Health monitoring via Spring Actuator
  - Prometheus metrics

## 🛠️ Technologies

- **Backend**
  - Java 21
  - Spring Boot
  - Spring Security
  - Spring Data JPA
  - GraphQL
  - JWT Authentication

- **Database & Caching**
  - PostgreSQL 16
  - Redis 6

- **DevOps & Deployment**
  - Docker
  - Docker Compose
  - GitHub Actions CI/CD
  - Spring Actuator for monitoring

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- PostgreSQL (if running locally)
- Redis (if running locally)

### Environment Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/qorelabs/user-service.git
   cd user-service
   ```

2. Create a `.env` file in the project root with the following variables:
   ```
   # Database
   DATASOURCE=jdbc:postgresql://localhost:5432/user_service
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=your_password

   # Security
   SERVICE_PASSWORD=your_service_password
   JWT_SECRET=your_jwt_secret
   JWT_EXPIRATION_MS=86400000
   JWT_REFRESH_EXPIRATION_MS=604800000
   API_KEY=your_api_key

   # Email
   MAIL_PASSWORD=your_mail_password

   # OAuth2
   GITHUB_CLIENT_ID=your_github_client_id
   GITHUB_CLIENT_SECRET=your_github_client_secret
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   OAUTH2_SUCCESS_REDIRECT_URL=http://localhost:5173/login/success
   ```

### Running Locally

#### Using Maven

```bash
# Build the application
./mvnw clean package

# Run with dev profile
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### Using Docker Compose

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f user-service

# Stop all services
docker-compose down
```

## 📚 API Documentation

### REST Endpoints

The service exposes the following key REST endpoints:

- **Authentication**
  - `POST /api/users/register` - Register a new user
  - `POST /api/users/login` - Authenticate a user
  - `POST /api/users/logout` - Logout a user
  - `POST /api/users/refresh-token` - Refresh JWT token
  - `GET /api/users/verify-email` - Verify user email
  - `POST /api/users/forgot-password` - Initiate password reset
  - `POST /api/users/reset-password` - Complete password reset

- **User Management**
  - `GET /api/users/{id}` - Get user by ID
  - `GET /api/users/all` - Get all users
  - `GET /api/users/search` - Search users
  - `PUT /api/users/{id}` - Update user
  - `DELETE /api/users/{id}` - Delete user

- **Admin Operations**
  - `GET /api/admin/users` - Admin access to all users
  - `PUT /api/admin/users/{id}/role` - Change user role

### GraphQL API

The service provides a GraphQL API at `/graphql` with GraphiQL interface available at `/graphiql` in development mode.

**Example Queries:**

```graphql
# Get user by ID
query {
  getUserById(id: "123e4567-e89b-12d3-a456-426614174000") {
    id
    firstName
    lastName
    email
    role
  }
}

# Search users
query {
  searchUsers(query: "john") {
    id
    firstName
    lastName
    email
  }
}
```

**Example Mutations:**

```graphql
# Create a new user
mutation {
  createUser(
    firstName: "John"
    lastName: "Doe"
    username: "johndoe"
    phoneNumber: "+1234567890"
    email: "john.doe@example.com"
    password: "securePassword123"
    role: TENANT
  ) {
    id
    firstName
    lastName
    email
  }
}
```

## ⚙️ Configuration

The application uses Spring profiles for different environments:

- `dev` - Development environment (default)
- `prod` - Production environment
- `test` - Testing environment

Key configuration files:

- `application-dev.properties` - Development configuration
- `application-prod.properties` - Production configuration
- `application-test.properties` - Test configuration

## 🔄 Development Workflow

1. Create a feature branch from `main`
2. Implement your changes
3. Write tests for your changes
4. Run tests locally
5. Create a pull request to `main`
6. CI/CD pipeline will run tests and checks
7. After approval and merge, the changes will be deployed automatically

## 📦 Deployment

The application is containerized and can be deployed using Docker Compose or to a Kubernetes cluster.

### Docker Compose Deployment

```bash
# Build and deploy
docker-compose up -d

# Scale if needed
docker-compose up -d --scale user-service=3
```

### CI/CD Pipeline

The repository includes GitHub Actions workflows for:

- Building and testing on pull requests
- Building, testing, and deploying on merges to main

## 🔍 Monitoring

The application exposes metrics and health information via Spring Actuator:

- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus metrics: `/actuator/prometheus`

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributors

- Doluwamu Kuye - Initial work and maintenance

## 📞 Support

For support, please contact support@qorelabs.org or open an issue on the GitHub repository.