# Relief Connect Forum — Version 2 (Enhanced Monolith)

Spring Boot application for disaster-relief coordination (rescue requests, fundraising, donations).

Version 2 introduces **JWT authentication**, **advanced querying with join-indexed tables**, and **layered architecture improvements** while maintaining monolithic deployment.

## Tech Stack
- **Backend**: Spring Boot 3.5.6, Spring Security, Spring Data JPA
- **Database**: PostgreSQL (UUID PKs, indexed joins)
- **Auth**: JWT (access + refresh tokens) with BCrypt password encoding
- **API Docs**: springdoc-openapi (Swagger UI)
- **Build**: Maven 3.x
- **IDE**: IntelliJ IDEA 2024.2.6
- **Java**: JDK 17+

---

## Architecture Evolution

### v1 → v2 Key Changes

| Aspect | v1 (Basic CRUD) | v2 (Enhanced Security & Querying) |
|--------|-----------------|-----------------------------------|
| **Authentication** | None (open endpoints) | JWT-based with role-based access (`ADMIN`, `USER`) |
| **Security** | CORS-only config | Spring Security with stateless sessions, `DaoAuthenticationProvider` |
| **Database** | Single `posts` table | Multi-table schema: `users`, `posts`, `donations` with join indexes |
| **Queries** | Simple JPA methods | Native SQL queries with `@Query`, pagination, aggregations |
| **DTOs** | Minimal request/response | Separated DTOs for auth (`LoginRequest`, `JwtResponse`) and domain operations |
| **Layering** | Basic MVC | Improved service layer with `AuthService`, `PostService`, `DonationService` |
| **Validation** | None | Jakarta Bean Validation (`@Email`, `@NotBlank`) |
| **Tokens** | N/A | Access token (24h) + Refresh token (7d) with expiration handling |

### Current Architecture (v2)
```
┌─────────────────────────────────────────────────────┐
│  Controllers (REST API)                             │
│  ├─ AuthController (/api/auth/*)                   │
│  ├─ PostController (/api/posts/*)                  │
│  └─ DonationController (planned)                   │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Security Layer                                      │
│  ├─ JwtRequestFilter (token validation)            │
│  ├─ JwtAuthenticationEntryPoint (401 handler)      │
│  └─ SecurityConfig (endpoint permissions)          │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Service Layer                                       │
│  ├─ AuthServiceImpl (login, register, refresh)     │
│  ├─ UserDetailsServiceImpl (load user by email)    │
│  └─ PostService, DonationService (business logic)  │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Repository Layer (JPA + Native Queries)            │
│  ├─ UserRepository (find by email/username)        │
│  ├─ PostRepository (search by place/places)        │
│  └─ DonationRepository (aggregations, join queries)│
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Database (PostgreSQL)                              │
│  ├─ users (id, email, password_hash, role)         │
│  ├─ posts (id, author_id, location, post_type)     │
│  └─ donations (id, user_id, post_id, amount)       │
│     Indexes: email, username, location, post_id    │
└─────────────────────────────────────────────────────┘
```

---

## Database Schema

### Tables
**users** (indexed: `email`, `username`)
```sql
id UUID PRIMARY KEY
username VARCHAR(100) UNIQUE NOT NULL
email VARCHAR(255) UNIQUE NOT NULL
password VARCHAR(255) NOT NULL
role VARCHAR(20) NOT NULL  -- 'ADMIN' | 'USER'
full_name VARCHAR(150)
phone VARCHAR(40)
address VARCHAR(200)
avatar_url VARCHAR(255)
bio VARCHAR(500)
post_count INT DEFAULT 0
created_at TIMESTAMP
```

**posts** (indexed: `author_id`, `location`)
```sql
id UUID PRIMARY KEY
author_id UUID REFERENCES users(id)
title VARCHAR(255) NOT NULL
description TEXT
post_type VARCHAR(20)  -- 'RESCUE' | 'FUNDRAISE' | 'UPDATE'
location VARCHAR(200)
contact_name VARCHAR(150)
contact_phone VARCHAR(40)
target_amount DECIMAL(15,2)
current_amount DECIMAL(15,2) DEFAULT 0
created_at TIMESTAMP
updated_at TIMESTAMP
```

**donations** (indexed: `post_id`, `user_id`)
```sql
id UUID PRIMARY KEY
post_id UUID REFERENCES posts(id)
user_id UUID REFERENCES users(id)
amount DECIMAL(15,2) NOT NULL
message TEXT
donated_at TIMESTAMP
```

---

## Security Features (NEW in v2)

### 1. JWT Authentication Flow
```
┌──────────┐    POST /api/auth/register    ┌──────────┐
│  Client  │ ───────────────────────────> │  Server  │
└──────────┘                                └──────────┘
                                                 │
                                                 ↓
                            ┌─────────────────────────────────┐
                            │ • Hash password (BCrypt)        │
                            │ • Store user in DB              │
                            │ • Return user details           │
                            └─────────────────────────────────┘

┌──────────┐    POST /api/auth/login       ┌──────────┐
│  Client  │ ───────────────────────────> │  Server  │
└──────────┘                                └──────────┘
                                                 │
                                                 ↓
                            ┌─────────────────────────────────┐
                            │ • Authenticate (email+password) │
                            │ • Generate access token (24h)   │
                            │ • Generate refresh token (7d)   │
                            │ • Return JWT response           │
                            └─────────────────────────────────┘

┌──────────┐    GET /api/posts              ┌──────────┐
│  Client  │ ───────────────────────────> │  Server  │
│  Header: │                                └──────────┘
│  Authorization: Bearer <token>                │
└──────────┘                                     ↓
                            ┌─────────────────────────────────┐
                            │ • JwtRequestFilter validates    │
                            │ • SecurityContext authenticated │
                            │ • Proceed to controller         │
                            └─────────────────────────────────┘
```

### 2. Endpoint Security Matrix

| Endpoint | Auth Required | Roles | Note |
|----------|---------------|-------|------|
| `POST /api/auth/register` | ❌ No | Public | Creates new user account |
| `POST /api/auth/login` | ❌ No | Public | Returns JWT tokens |
| `POST /api/auth/refresh` | ❌ No | Public | Renews access token |
| `POST /api/auth/logout` | ❌ No | Public | Invalidates refresh token |
| `GET /api/posts` | ✅ Yes | `USER`, `ADMIN` | List posts with pagination |
| `POST /api/posts` | ✅ Yes | `USER`, `ADMIN` | Create new post |
| `PUT /api/posts/{id}` | ✅ Yes | `USER`, `ADMIN` | Update own post |
| `DELETE /api/posts/{id}` | ✅ Yes | `ADMIN` | Delete any post |
| `/swagger-ui/**` | ❌ No | Public | API documentation |

### 3. JWT Configuration
```yaml
# application-dev.yml
jwt:
  secret: mySecretKey123456789012345678901234567890123456789012345678901234567890
  access-token:
    expiration: 86400000    # 24 hours in ms
  refresh-token:
    expiration: 604800000   # 7 days in ms
```

---

## Advanced Query Features (NEW in v2)

### DonationRepository Examples

**1. Aggregate donations by post**
```java
@Query(value = "SELECT d.post_id as postId, SUM(d.amount) as totalAmount " +
               "FROM donations d GROUP BY d.post_id",
       countQuery = "SELECT COUNT(DISTINCT d.post_id) FROM donations d",
       nativeQuery = true)
Page<Object[]> findDonationStatistics(Pageable pageable);
```

**2. Filter by location with joins**
```java
@Query(value = "SELECT d.* FROM donations d " +
               "LEFT JOIN posts p ON d.post_id = p.id " +
               "WHERE p.location = :location",
       nativeQuery = true)
Page<Donation> findByLocationWithDetails(@Param("location") String location, Pageable pageable);
```

**3. Multi-location search**
```java
Page<Donation> findByLocationsWithDetails(@Param("locations") String[] locations, Pageable pageable);
```

---

## Run Locally (Windows)

### Prerequisites
- JDK 17+
- Maven 3.9+
- PostgreSQL 14+ with database `reliefV2`

### Steps
1. **Configure database** in `src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your-database-name
    username: your-username
    password: your-password
```

2. **Build and run**:
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

3. **Access endpoints**:
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - API Docs: http://localhost:8080/v3/api-docs

---

## API Usage Examples

### 1. Register New User
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "0123456789",
  "address": "Ha Noi"
}
```

### 2. Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}

# Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "john@example.com",
  "role": "USER"
}
```

### 3. Access Protected Endpoint
```bash
GET http://localhost:8080/api/posts?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Refresh Token
```bash
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 5. Search Posts by Location
```bash
GET http://localhost:8080/api/posts/search/place?place=Ha%20Noi&page=0&size=10
Authorization: Bearer <your-token>
```

---

## Version 2 Improvements Summary

### ✅ Implemented
- **Layered split**: Auth service, Post service, Donation service
- **Security**: JWT authentication with access/refresh tokens
- **User Management**: Registration, login, role-based authorization
- **Password Security**: BCrypt hashing with salt
- **Token Management**: Automatic expiration, refresh mechanism
- **Database**: Multi-table schema with foreign keys and indexes
- **Queries**: Native SQL for complex joins and aggregations
- **Validation**: Bean validation on DTOs
- **API Docs**: Swagger UI with Bearer token authentication
- **Filter Optimization**: `shouldSkipFilter()` for public endpoints
- **Error Handling**: Custom 401 entry point, structured responses

### 🚧 Limitations (To be addressed in v3)
- No token blacklisting for logout (requires Redis/cache)
- No pagination helpers for consistent responses
- Missing global exception handler
- No audit logging for security events
- CORS still permits all origins (needs refinement)
- No rate limiting on auth endpoints
- Missing user profile update endpoints
- No email verification on registration

### 🎯 Planned for Version 3 (Full Layered Architecture)
- **Message queue**: RabbitMQ/Kafka for async operations
- **Caching**: Redis for token blacklist and session management
- **File upload**: S3/Cloudinary for avatars and post images
- **Notifications**: Email/SMS for donation confirmations
- **Analytics**: Donation statistics dashboard
- **Testing**: Unit tests (JUnit 5), integration tests (Testcontainers)
- **CI/CD**: GitHub Actions pipeline
- **Monitoring**: Actuator + Prometheus/Grafana

---

## Version 1 Overview (Basic CRUD)

Version 1 was a simple monolithic application with:
- Basic CRUD operations for posts
- No authentication/authorization
- Single `posts` table in PostgreSQL
- Simple JPA repository methods
- CORS configuration only
- Manual UUID generation
- Basic Swagger documentation

**Key differences from v2:** 
- Open endpoints (no security)
- No user management
- No token-based authentication
- Simple database schema
- Basic error responses
- No pagination support
- No query optimization

---

## Release Tags

### v1.0.0 (Basic CRUD)
```bash
git tag -a v1.0.0 -m "release: v1.0.0 - Basic post management"
git push origin v1.0.0
```

### v2.0.0 (JWT + Advanced Queries)
```bash
git tag -a v2.0.0 -m "release: v2.0.0 - JWT auth, join queries, layered architecture"
git push origin v2.0.0
```

---

## License
MIT License - See LICENSE file for details

## Contact
- GitHub: [@LouisVie61](https://github.com/LouisVie61)
- Project: [ReliefConnectForum](https://github.com/LouisVie61/ReliefConnectForum)

