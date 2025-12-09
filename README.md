# Relief Connect Forum — Version 3 (Monolithics with AI-Worker)

Spring Boot application for disaster-relief coordination (rescue requests, fundraising, donations) with advanced session management, full-text search, OAuth2 authentication, and **AI-powered post classification**.

Version 3 introduces **AI-Worker Monolithic** for intelligent post classification, Redis-based job queue, and communication patterns.

---

## Tech Stack

### Core Framework
- **Backend**: Spring Boot 3.5.6
- **Security**: Spring Security with JWT & OAuth2
- **Data Access**: Spring Data JPA
- **Validation**: Jakarta Bean Validation
- **Build Tool**: Maven 3.x
- **Java Version**: JDK 17+

### Data Layer
- **Primary Database**: PostgreSQL 15+ (UUID PKs, indexed joins)
- **Search Engine**: Elasticsearch 8.11.0 (full-text search with accent-folding)
- **Cache & Sessions**: Redis 7 (JWT token management, session limiting)
- **Message Queue**: Redis Lists (AI job queue)
- **Connection Pooling**: HikariCP (default)

### AI & Machine Learning
- **AI-Worker**: Separate Monolithic (Python/Node.js)
- **Job Queue**: Redis List (`ai_jobs_queue`)
- **Classification**: NLP-based post type detection (RESCUE, FUNDRAISE, UPDATE)
- **Communication**: Asynchronous job processing

### Authentication & Authorization
- **JWT**: JJWT 0.11.5 (access + refresh tokens)
- **Password Hashing**: BCrypt
- **OAuth2**: Google OAuth2 Client
- **Session Management**: Redis-backed with max 5 sessions per user

### API & Documentation
- **API Docs**: springdoc-openapi 2.2.0 (Swagger UI)
- **REST**: Spring Web MVC
- **WebSocket**: Spring WebSocket (future real-time features)

### DevOps & Deployment
- **Containerization**: Docker & Docker Compose
- **IDE**: IntelliJ IDEA 2024.2.6
- **Testing**: JUnit 5, Spring Security Test

---

## Testing for the latest version

### Load Testing Results - Redis Session Management

Real-world performance metrics under concurrent load with JWT + Redis session management:

| Concurrent Users | RPS/User | Total RPS | Avg Latency (ms) | P95 (ms) | Error Rate (%) | 429 Rate (%) | Redis CPU (%) | Status    |
|----------------:|----------:|----------:|-----------------:|---------:|---------------:|-------------:|--------------:|-----------|
| 100             | 0.5       | 50        | 40               | 90       | 0.05           | 0.1          | 15            |  Excellent |
| 300             | 0.5       | 150       | 55               | 120      | 0.1            | 0.3          | 35            |  Good     |
| 500             | 0.5       | 250       | 70               | 160      | 0.2            | 0.6          | 50            |  Acceptable |
| **700**         | **0.5**   | **350**   | **80**           | **200**  | **0.5**        | **2.0**      | **60**        |  **Optimal** |
| 800             | 0.5       | 400       | 120              | 350      | 3.0            | 8.0          | 85            |  Degraded |

**Test Configuration:**
- **Hardware**: 8-core CPU, 16GB RAM, SSD storage
- **Database**: PostgreSQL
- **Cache**: Redis 7 with default configuration
- **Test Tool**: Locust with realistic user scenarios
- **Duration**: 10-minute sustained load per test

**Key Findings:**
- **Sweet Spot**: 700 concurrent users at 350 RPS
- **Linear Scaling**: Up to 700 users with <1% error rate
- **Sub-100ms Latency**: P95 under 200ms at optimal load
- **Session Limit Working**: 2% rate limiting at peak (by design)
- **Redis Efficiency**: 60% CPU at 350 RPS (room for growth)

**Scalability Recommendations:**
- Add Redis Cluster for >1000 concurrent users
- Implement read replicas for PostgreSQL at >500 RPS
- Consider horizontal scaling (multiple app instances) at >700 users
- Enable connection pooling tuning for >400 RPS

---

## Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│  Controller Layer (REST Endpoints)                              │
│  ├─ AuthController         (/api/auth/*)                        │
│  ├─ UserController         (/api/users/*)                       │
│  ├─ PostController         (/api/posts/*)                       │
│  ├─ PostSearchController   (/api/search/posts/*)                │
│  ├─ DonationController     (/api/donations/*)                   │
│  ├─ AdminController        (/api/admin/*)                       │
│  └─ OAuth2TestController   (/test/*)                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Security & Filter Layer                                        │
│  ├─ JwtRequestFilter         (validates JWT on each request)    │
│  ├─ JwtAuthenticationEntryPoint (handles 401 errors)            │
│  ├─ SecurityConfig           (endpoint permissions)             │
│  └─ JwtUtil                  (token generation & parsing)       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Service Layer (Business Logic)                                 │
│  ├─ auth/                                                       │
│  │   ├─ AuthService         (login, register, refresh)          │
│  │   └─ JWTTokenService     (Redis token management)            │
│  ├─ core/                                                       │
│  │   ├─ UserService         (user profile operations)           │
│  │   ├─ PostService         (post CRUD)                         │
│  │   ├─ PostSearchService   (Elasticsearch queries)             │
│  │   ├─ PostDocService      (sync to Elasticsearch)             │
│  │   ├─ DonationService     (donation processing)               │
│  │   └─ AdminService        (analytics & management)            │
│  ├─ event/                                                      │
│  │   ├─ AIJobService        (async AI classification)           │
│  │   ├─ TokenEventListener  (handles token revocation events)   │
│  │   └─ PostCreatedEvent    (event publishing)                  │
│  └─ OtherParties/           (third-party integrations)          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Repository Layer                                               │
│  ├─ UserRepository          (JPA + custom queries)              │
│  ├─ PostRepository          (JPA + native SQL)                  │
│  ├─ DonationRepository      (aggregations, joins)               │
│  └─ doc/PostDocRepository   (Elasticsearch repository)          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Data Layer                                                     │
│  ├─ PostgreSQL (users, posts, donations)                        │
│  ├─ Redis (JWT tokens, sessions, pub/sub)                       │
│  └─ Elasticsearch (posts index with folding analyzer)           │
└─────────────────────────────────────────────────────────────────┘
```

### Event-Driven Components
```
Post Creation Flow with AI-Worker:
┌──────────┐  create()  ┌─────────────┐  publish  ┌─────────────────┐
│  Client  │ ────────>  │ PostService │ ────────> │ PostCreatedEvent│
└──────────┘            └─────────────┘           └─────────────────┘
                              │                            │
                              ↓                            ↓
                        ┌─────────────┐            ┌────────────────┐
                        │ PostgreSQL  │            │ AIJobService   │
                        │ (PENDING)   │            │ @Async         │
                        └─────────────┘            └────────────────┘
                                                           │
                                                           ↓
                                                   ┌────────────────┐
                                                   │  Redis Queue   │
                                                   │ ai_jobs_queue  │
                                                   │ RPUSH job      │
                                                   └────────────────┘
                                                           │
                                                           ↓ BLPOP
                                                   ┌────────────────┐
                                                   │  AI-Worker     │
                                                   │ (Python/Node)  │
                                                   │ - NLP Model    │
                                                   │ - Classify     │
                                                   └────────────────┘
                                                           │
                                                           ↓ HTTP PUT
                                                   ┌────────────────┐
                                                   │ PostService    │
                                                   │ Update type    │
                                                   └────────────────┘
                                                           │
                                                           ↓
                                        ┌──────────────────┴──────────────────┐
                                        ↓                                     ↓
                                ┌─────────────┐                    ┌────────────────┐
                                │ PostgreSQL  │                    │ PostDocService │
                                │ (CLASSIFIED)│                    │ → Elasticsearch│
                                └─────────────┘                    └────────────────┘

Token Revocation Flow:
┌──────────┐  logout  ┌──────────────┐  publish  ┌────────────────────┐
│  Client  │ ───────> │ AuthService  │ ────────> │ TokenRevokedEvent  │
└──────────┘          └──────────────┘           └────────────────────┘
                              │                            │
                              ↓                            ↓
                        ┌───────────┐            ┌──────────────────────┐
                        │   Redis   │            │ TokenEventListener   │
                        │ DEL token │            │ (cleans up sessions) │
                        └───────────┘            └──────────────────────┘
```

---

## Database Schema

### Entity Relationship Diagram
```
┌──────────────────┐          ┌──────────────────┐
│      User        │          │      Post        │
├──────────────────┤          ├──────────────────┤
│ id (UUID) PK     │──────<   │ id (UUID) PK     │
│ username (unique)│    1:N   │ user_id FK       │
│ email (unique)   │          │ title            │
│ password         │          │ content          │
│ role             │          │ post_type        │
│ provider         │          │ location         │
│ avatar_url       │          │ contact_name     │
│ bio              │          │ contact_phone    │
│ post_count       │          │ target_amount    │
│ created_at       │          │ current_amount   │
└──────────────────┘          │ created_at       │
        │                     │ updated_at       │
        │                     └──────────────────┘
        └──────────┬───────────────────┘
                   │
                   │ N:M (through Donation)
                   ↓
            ┌──────────────────┐
            │    Donation      │
            ├──────────────────┤
            │ id (UUID) PK     │
            │ user_id FK       │
            │ post_id FK       │
            │ amount           │
            │ currency         │
            │ note             │
            │ location         │
            │ created_at       │
            └──────────────────┘
```

### Table Definitions

**users**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),                    -- nullable for OAuth2 users
    role VARCHAR(20) NOT NULL,                -- 'ADMIN' | 'USER'
    provider VARCHAR(50),                     -- 'GOOGLE' | 'NULL'
    full_name VARCHAR(150),
    phone VARCHAR(40),
    address VARCHAR(200),
    avatar_url VARCHAR(255),
    bio VARCHAR(500),
    post_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

**posts**
```sql
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(140) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    post_type VARCHAR(20),                    -- 'RESCUE' | 'FUNDRAISE' | 'UPDATE' | 'PENDING'
    location VARCHAR(255),
    contact_name VARCHAR(120),
    contact_phone VARCHAR(40),
    target_amount DECIMAL(19,2),
    current_amount DECIMAL(19,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_posts_title ON posts(title);
CREATE INDEX idx_posts_post_type ON posts(post_type);
CREATE INDEX idx_posts_location ON posts(location);
```

**donations**
```sql
CREATE TABLE donations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    post_id UUID NOT NULL REFERENCES posts(id),
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    note VARCHAR(500),
    location VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_donations_user ON donations(user_id);
CREATE INDEX idx_donations_post ON donations(post_id);
```

### Elasticsearch Index

**posts** (Elasticsearch)
```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "folding": {
          "tokenizer": "standard",
          "filter": ["lowercase", "asciifolding"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "title": { "type": "text", "analyzer": "folding" },
      "content": { "type": "text", "analyzer": "folding" },
      "postType": { "type": "keyword" },
      "location": { "type": "text", "analyzer": "folding" },
      "authorId": { "type": "keyword" },
      "authorUsername": { "type": "text", "analyzer": "folding" },
      "targetAmount": { "type": "double" },
      "currentAmount": { "type": "double" },
      "createdAt": { "type": "date", "format": "uuuu-MM-dd'T'HH:mm:ss" }
    }
  }
}
```

---

## Security Features

### 1. JWT Authentication with Redis Session Management

**Token Types:**
- **Access Token**: 24 hours validity, used for API authentication
- **Refresh Token**: 7 days validity, used to obtain new access tokens

**Redis Session Control:**
- Maximum 5 concurrent sessions per user
- Automatic eviction of oldest session when limit exceeded
- Lua script for atomic session enforcement
- Token prefix mapping: `jwt:token:{token}` → user data
- User tokens tracking: `jwt:user:{userId}` → set of active tokens
- Reverse mapping: `jwt:token-user:{token}` → userId

**Session Limit Enforcement (Lua Script):**
```lua
-- enforce-session-limit.lua
-- Atomically checks session count and revokes oldest if limit exceeded
-- Returns: {1, oldestToken} if revoked, {0, ''} if within limit
```

### 2. Authentication Flow

**Registration:**
```
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "0123456789",
  "address": "Ha Noi"
}

Response:
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "createdAt": "2024-12-08T10:30:00"
}
```

**Login (Local):**
```
POST /api/auth/login
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "uuid",
  "email": "john@example.com",
  "username": "john_doe",
  "role": "USER"
}
```

**OAuth2 Login (Google):**
```
1. GET /test/login-with-google → Returns OAuth2 URL
2. User visits: http://localhost:8080/oauth2/authorization/google
3. Google authentication flow
4. Redirect: http://localhost:8080/login/oauth2/code/google
5. Returns JWT tokens in response
```

**Token Refresh:**
```
POST /api/auth/refresh?refreshToken={token}

Response: New access token + same refresh token
```

### 3. Endpoint Security Matrix

| Endpoint | Auth | Roles | Description |
|----------|------|-------|-------------|
| **Authentication** |
| `POST /api/auth/register` | ❌ | Public | Create new account |
| `POST /api/auth/login` | ❌ | Public | Login with credentials |
| `POST /api/auth/refresh` | ❌ | Public | Refresh access token |
| `GET /oauth2/authorization/google` | ❌ | Public | Initiate Google OAuth2 |
| **Users** |
| `GET /api/users/{id}` | ✅ | USER, ADMIN | Get user profile |
| `PUT /api/users/{id}` | ✅ | USER, ADMIN | Update profile |
| `POST /api/users/{id}/activate` | ✅ | USER, ADMIN | Activate account with OTP |
| **Posts** |
| `GET /api/posts` | ✅ | USER, ADMIN | List posts (paginated) |
| `GET /api/posts/{id}` | ✅ | USER, ADMIN | Get single post |
| `POST /api/posts` | ✅ | USER, ADMIN | Create post (triggers AI) |
| `PUT /api/posts/{id}` | ✅ | USER, ADMIN | Update post |
| `DELETE /api/posts/{id}` | ✅ | ADMIN | Delete post |
| **Search (Elasticsearch)** |
| `GET /api/search/posts/all` | ✅ | USER, ADMIN | Full-text search |
| `GET /api/search/posts/title` | ✅ | USER, ADMIN | Search by title |
| `GET /api/search/posts/content` | ✅ | USER, ADMIN | Search by content |
| `GET /api/search/posts/location` | ✅ | USER, ADMIN | Search by location |
| `GET /api/search/posts/type` | ✅ | USER, ADMIN | Filter by post type |
| **Donations** |
| `GET /api/donations` | ✅ | USER, ADMIN | List donations |
| `GET /api/donations/{id}` | ✅ | USER, ADMIN | Get donation |
| `POST /api/donations` | ✅ | USER, ADMIN | Create donation |
| `PUT /api/donations/{id}` | ✅ | USER, ADMIN | Update donation |
| **Admin** |
| `GET /api/admin/donations/statistics/*` | ✅ | ADMIN | Donation analytics |
| `GET /api/admin/donations/search/location` | ✅ | ADMIN | Filter by location |
| **Documentation** |
| `/swagger-ui/**` | ❌ | Public | Interactive API docs |
| `/v3/api-docs/**` | ❌ | Public | OpenAPI specification |

---

## Advanced Features

### 1. Elasticsearch Full-Text Search

**Accent-Insensitive Matching:**
- Searches "Hà Nội" will match "Ha Noi", "Hanoi", "hà nội"
- Uses custom `folding` analyzer with `asciifolding` filter

**Search Endpoints:**
```java
// Search across title AND content
GET /api/search/posts/all?query=lũ lụt&page=0&size=10

// Search only title
GET /api/search/posts/title?query=cứu trợ

// Search by location
GET /api/search/posts/location?location=Hà Nội

```

**Automatic Sync:**
- Post creation triggers `PostCreatedEvent`
- `AIJobService` processes asynchronously
- `PostDocService` indexes to Elasticsearch

### 2. Redis Session Management

**Features:**
- Token storage with TTL matching JWT expiration
- Session limit enforcement (max 5 per user)
- Atomic operations using Lua scripts
- Pub/Sub for token revocation events
- Reverse mapping for fast user lookups

**Configuration:**
```yaml
jwt:
  redis:
    token-prefix: "jwt:token:"
    user-tokens-prefix: "jwt:user:"
    reverse-map-prefix: "jwt:token-user:"
    max-sessions-per-user: 5
    revocation-batch-size: 1000
```

### 3. Event-Driven Architecture

**Events:**
- `PostCreatedEvent`: Published when new post is created
- `TokenRevokedEvent`: Published when token is invalidated
- `UserAllTokensRevokedEvent`: Published on logout all devices

**Listeners:**
- `TokenEventListener`: Handles token cleanup
- `RedisKeyExpirationListener`: Handles expired token cleanup
- `AIJobService`: Processes post classification asynchronously

**Async Configuration:**
```java
@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

### 4. Admin Analytics

**Donation Statistics:**
```java
// Total donations per post
GET /api/admin/donations/statistics/post/{postId}

// All posts with donation stats (paginated)
GET /api/admin/donations/statistics/all-posts?page=0&size=20

```

**Native SQL Aggregations:**
```java
@Query(value = "SELECT d.post_id as postId, SUM(d.amount) as totalAmount " +
               "FROM donations d GROUP BY d.post_id",
       countQuery = "SELECT COUNT(DISTINCT d.post_id) FROM donations d",
       nativeQuery = true)
Page<Object[]> findDonationStatistics(Pageable pageable);
```

---

## Configuration

### Application Profiles

**application.properties**
```properties
spring.application.name=ReliefConnectForum
spring.profiles.active=dev
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
spring.session.store-type=none
```

**application-dev.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/reliefV2
    username: postgres
    password: 123456
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  elasticsearch:
    uris: http://localhost:9201
    connection-timeout: 5s
    socket-timeout: 30s

  data:
    redis:
      host: localhost
      port: 6380
      connect-timeout: 6000ms
      timeout: 6000ms
      database: 0
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-client-id
            client-secret: your-client-secret
            scope: profile, email
            redirect-uri: http://localhost:8080/login/oauth2/code/google

jwt:
  secret: mySecretKey123456789012345678901234567890123456789012345678901234567890
  access-token:
    expiration: 86400000    # 24 hours
  refresh-token:
    expiration: 604800000   # 7 days
  redis:
    token-prefix: "jwt:token:"
    user-tokens-prefix: "jwt:user:"
    reverse-map-prefix: "jwt:token-user:"
    max-sessions-per-user: 5

server:
  port: 8080
  error:
    include-message: always
```

---

## Docker Deployment

### Docker Compose Setup

**docker-compose.yml** includes:
- PostgreSQL 15 (port 5432)
- Redis 7 (port 6380)
- Elasticsearch 8.11.0 (port 9201)
- Spring Boot Application (port 8080)
- AI-Worker (Python/Node.js)

**Start all services:**
```cmd
docker-compose up -d
```

**Check service health:**
```cmd
docker-compose ps
```

**View application logs:**
```cmd
docker-compose logs -f app
```

**Stop all services:**
```cmd
docker-compose down
```

**Clean volumes:**
```cmd
docker-compose down -v
```

### Service Dependencies

The application waits for:
- PostgreSQL health check (pg_isready)
- Redis health check (redis-cli ping)
- Elasticsearch health check (_cluster/health)
- AI-Worker readiness (HTTP check)

---

## Local Development Setup

### Prerequisites
1. **JDK 17+** ([Download](https://adoptium.net/))
2. **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
3. **Docker Desktop** (for running services)

### Quick Start

**1. Start infrastructure services:**
```cmd
docker-compose up -d postgres redis elasticsearch
```

**2. Configure application:**
Edit `src/main/resources/application-dev.yml` if needed.

**3. Build the project:**
```cmd
mvnw clean install
```

**4. Run the application:**
```cmd
mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**5. Access the application:**
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs

### Testing Endpoints

**Using Swagger UI:**
1. Open http://localhost:8080/swagger-ui/index.html
2. Register a new user via `/api/auth/register`
3. Login via `/api/auth/login` to get JWT token
4. Click "Authorize" button and enter: `Bearer {your-access-token}`
5. Test protected endpoints

**Using cURL:**
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"john\",\"email\":\"john@example.com\",\"password\":\"Pass123!\"}"

# Login
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"john@example.com\",\"password\":\"Pass123!\"}"

# Get posts (with token)
curl -X GET http://localhost:8080/api/posts ^
  -H "Authorization: Bearer {your-token}"

# Search posts
curl -X GET "http://localhost:8080/api/search/posts/all?query=rescue" ^
  -H "Authorization: Bearer {your-token}"
```

---

## API Examples

### Complete User Journey

**1. Register New User**
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "rescuer01",
  "email": "rescuer@relief.vn",
  "password": "SecurePass123!",
  "fullName": "Nguyen Van A",
  "phone": "0912345678",
  "address": "Ha Noi"
}
```

**2. Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "rescuer@relief.vn",
  "password": "SecurePass123!"
}

Response:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "a1b2c3d4-...",
  "email": "rescuer@relief.vn",
  "username": "rescuer01",
  "role": "USER"
}
```

**3. Create Rescue Post**
```http
POST /api/posts
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "title": "Urgent: Family trapped by flood",
  "content": "5 people including elderly and children need immediate evacuation. Water level rising fast.",
  "postType": "RESCUE",
  "location": "Ward 3, District 5, Ho Chi Minh City",
  "contactName": "Tran Thi B",
  "contactPhone": "0987654321",
  "targetAmount": null
}

Response:
{
  "post": {
    "id": "uuid",
    "title": "Urgent: Family trapped by flood",
    "postType": "PENDING",
    "createdAt": "2024-12-08T15:30:00"
  },
  "message": "Post created successfully. AI classification is processing in the background."
}
```

**4. Search Posts**
```http
GET /api/search/posts/all?query=flood&page=0&size=10
Authorization: Bearer eyJhbGc...

Response: Page<PostSearchResponse>
```

**5. Create Donation**
```http
POST /api/donations
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "postId": "uuid-of-post",
  "amount": 500000,
  "currency": "VND",
  "message": "Stay strong!",
  "location": "Ha Noi"
}
```

**6. View Analytics (Admin)**
```http
GET /api/admin/donations/statistics/all-posts?page=0&size=20
Authorization: Bearer {admin-token}

Response:
{
  "content": [
    {
      "postId": "uuid",
      "totalAmount": 15000000
    }
  ]
}
```

---

## Version History & Evolution

### v1.0.0 (Basic CRUD) - Initial Release
**Features:**
- Basic post management (CRUD)
- Simple PostgreSQL schema
- No authentication
- CORS configuration only
- Manual UUID generation

**Limitations:**
- No security layer
- No user management
- No search capabilities
- No caching
- No async processing


### v2.0.0 (Auth & Database)
**Features:** 
- Architecture: Monolithic Layered Advanced
- User authentication (JWT + BCrypt)
- Database: PostgreSQL with relational schema + JOIN Table + Indexing
---

### v3.1.0 (Current) - Enhanced Monolith

**Major Additions:**

✅ **Security & Authentication**
- JWT-based authentication (access + refresh tokens)
- Redis-backed session management with 5-session limit
- BCrypt password hashing
- OAuth2 Google login integration
- Role-based access control (USER, ADMIN)
- Lua scripts for atomic Redis operations

✅ **Search & Performance**
- Elasticsearch full-text search with accent-folding
- Automatic post indexing via events
- Custom analyzers for Vietnamese text
- Paginated search results

✅ **Architecture Improvements**
- Event-driven architecture (Spring Events)
- Async processing with thread pools
- Service layer separation (auth, core, event, OtherParties)
- DTO pattern for all request/response
- Bean validation on inputs

✅ **Data Layer**
- Multi-table schema (users, posts, donations)
- Foreign key relationships
- Strategic indexes on high-query columns
- Native SQL for complex aggregations

✅ **Admin Features**
- Donation statistics and analytics
- Location-based filtering
- User management endpoints
- Post moderation capabilities

✅ **DevOps**
- Docker Compose orchestration
- Health checks for all services
- Environment-based configuration
- Production-ready error handling

✅ **API Documentation**
- Swagger UI with Bearer token support
- OpenAPI 3.0 specification
- Comprehensive endpoint descriptions
- Example requests/responses

---

## Key Design Decisions

### 1. Why Redis for Sessions?
- **Atomic operations**: Lua scripts ensure consistent session limits
- **Fast lookups**: O(1) token validation
- **TTL support**: Automatic token expiration
- **Pub/Sub**: Event-driven token revocation
- **Scalability**: Easy to add Redis Cluster later

### 2. Why Elasticsearch?
- **Full-text search**: PostgreSQL LIKE queries don't scale
- **Accent-insensitive**: Critical for Vietnamese text
- **Fuzzy matching**: Handles typos gracefully
- **Fast**: Sub-second queries on millions of records
- **Analytics**: Aggregations for future dashboards

### 3. Why Event-Driven Architecture?
- **Decoupling**: PostService doesn't depend on ElasticsearchService
- **Async processing**: AI classification doesn't block user
- **Extensibility**: Easy to add email notifications, webhooks, etc.
- **Fault tolerance**: Failed events can be retried

### 4. Why Monolith (for now)?
- **Simplicity**: Single deployment unit
- **Development speed**: No network overhead between services
- **Team size**: Suitable for small teams
- **Easy debugging**: Single codebase
- **Future-ready**: Layered architecture enables easy Monolithics split

---

## Performance Considerations

### Database Indexes
- `users.email`: Login queries
- `users.username`: Profile lookups
- `posts.location`: Location-based search
- `posts.post_type`: Filter by type
- `donations.post_id`: Aggregation queries
- `donations.user_id`: User donation history

### Redis Optimization
- TTL on all tokens (automatic cleanup)
- Lua scripts (atomic operations, less round trips)
- Connection pooling (Lettuce)
- Pipelining for batch operations

### Elasticsearch Tuning
- Custom analyzer reduces index size
- Pagination prevents memory issues
- Field-specific queries (faster than match_all)

### Async Processing
- Thread pool for AI jobs (4-8 threads)
- Non-blocking post creation
- Event listeners run in separate threads

---

## Security Best Practices

✅ **Implemented:**
- Password hashing (BCrypt with salt)
- JWT token signing (HS256)
- HTTPS-ready (configure in production)
- SQL injection prevention (JPA parameterized queries)
- XSS prevention (Spring Security defaults)
- CORS configuration
- Rate limiting via Redis (session limits)

⚠️ **Production Recommendations:**
- Use environment variables for secrets
- Enable HTTPS/TLS
- Add rate limiting on auth endpoints
- Implement IP whitelisting for admin routes
- Enable Redis AUTH
- Use read replicas for PostgreSQL
- Set up Elasticsearch security (X-Pack)
- Rotate JWT secret regularly

---

## Testing

### Manual Testing
- **Swagger UI**: Interactive API testing
- **Postman Collection**: (to be added)

### Automated Testing (Planned)
```
src/test/java/
├─ integration/
│  ├─ AuthControllerIntegrationTest
│  ├─ PostControllerIntegrationTest
│  └─ ElasticsearchIntegrationTest
├─ service/
│  ├─ AuthServiceTest
│  ├─ PostServiceTest
│  └─ JWTTokenServiceTest
└─ repository/
   ├─ UserRepositoryTest
   └─ DonationRepositoryTest
```

**Future Testing Stack:**
- JUnit 5 for unit tests
- Testcontainers for integration tests (PostgreSQL, Redis, Elasticsearch)
- MockMvc for controller tests
- H2 for lightweight repository tests

---

## Troubleshooting

### Common Issues

**1. Application won't start - Port already in use**
```cmd
# Check what's using port 8080
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <process-id> /F
```

**2. Cannot connect to PostgreSQL**
```
- Check Docker container: docker ps
- Verify port mapping: 5432:5432
- Test connection: docker exec -it relief-postgres psql -U postgres -d reliefV2
```

**3. Redis connection failed**
```
- Check Redis container: docker logs relief-redis
- Verify port: 6380 (not default 6379)
- Test connection: docker exec -it relief-redis redis-cli ping
```

**4. Elasticsearch not indexing posts**
```
- Check ES health: curl http://localhost:9201/_cluster/health
- Verify index exists: curl http://localhost:9201/posts
- Check application logs for indexing errors
```

**5. JWT token not working**
```
- Ensure token is in Authorization header
- Format: "Bearer {token}" (with space)
- Check token expiration
- Verify Redis connection (session might be revoked)
```

---

## Project Structure

```
ReliefConnectForum/
├─ src/main/
│  ├─ java/demo/reliefconnectforum/
│  │  ├─ ReliefConnectForumApplication.java
│  │  ├─ config/
│  │  │  ├─ AsyncConfig.java
│  │  │  ├─ ElasticsearchConfig.java
│  │  │  ├─ JwtAuthenticationEntryPoint.java
│  │  │  ├─ JwtRedisProperties.java
│  │  │  ├─ JwtRequestFilter.java
│  │  │  ├─ JwtUtil.java
│  │  │  ├─ RedisConfig.java
│  │  │  ├─ SecurityConfig.java
│  │  │  ├─ SwaggerConfig.java
│  │  │  └─ WebConfig.java
│  │  ├─ controller/
│  │  │  ├─ AdminController.java
│  │  │  ├─ AuthController.java
│  │  │  ├─ DonationController.java
│  │  │  ├─ OAuth2TestController.java
│  │  │  ├─ PostController.java
│  │  │  ├─ PostSearchController.java
│  │  │  └─ UserController.java
│  │  ├─ dto/
│  │  │  ├─ request/
│  │  │  │  ├─ UserLoginRequest
│  │  │  │  ├─ UserRegisterRequest
│  │  │  │  ├─ PostRequest
│  │  │  │  └─ DonationRequest
│  │  │  └─ response/
│  │  │     ├─ UserLoginResponse
│  │  │     ├─ UserRegisterResponse
│  │  │     ├─ PostResponse
│  │  │     ├─ PostSearchResponse
│  │  │     ├─ DonationResponse
│  │  │     └─ DonationStatistic
│  │  ├─ entity/
│  │  │  ├─ User.java
│  │  │  ├─ Post.java
│  │  │  ├─ Donation.java
│  │  │  └─ doc/
│  │  │     └─ PostDoc.java (Elasticsearch entity)
│  │  ├─ Enum/
│  │  │  ├─ AuthProviderEnum.java (GOOGLE, NULL)
│  │  │  ├─ PostType.java (RESCUE, FUNDRAISE, UPDATE, PENDING)
│  │  │  └─ UserRoleEnum.java (USER, ADMIN)
│  │  ├─ listener/
│  │  │  └─ RedisKeyExpirationListener.java
│  │  ├─ repository/
│  │  │  ├─ UserRepository.java
│  │  │  ├─ PostRepository.java
│  │  │  ├─ DonationRepository.java
│  │  │  └─ doc/
│  │  │     └─ PostDocRepository.java (Elasticsearch)
│  │  ├─ scheduler/
│  │  │  └─ CacheEvictionScheduler.java (commented out)
│  │  ├─ security/
│  │  ├─ service/
│  │  │  ├─ auth/
│  │  │  │  ├─ AuthService.java
│  │  │  │  ├─ impl/AuthServiceImpl.java
│  │  │  │  └─ JWTTokenService.java
│  │  │  ├─ core/
│  │  │  │  ├─ AdminService.java
│  │  │  │  ├─ DonationService.java
│  │  │  │  ├─ PostService.java
│  │  │  │  ├─ PostSearchService.java
│  │  │  │  ├─ PostDocService.java
│  │  │  │  ├─ UserService.java
│  │  │  │  └─ impl/
│  │  │  ├─ event/
│  │  │  │  ├─ AIJobService.java
│  │  │  │  ├─ PostCreatedEvent.java
│  │  │  │  ├─ TokenEventListener.java
│  │  │  │  ├─ TokenRevokedEvent.java
│  │  │  │  └─ UserAllTokensRevokedEvent.java
│  │  │  └─ OtherParties/
│  │  └─ ...
│  └─ resources/
│     ├─ application.properties
│     ├─ application-dev.yml
│     ├─ application-test.yml
│     ├─ data.sql
│     ├─ elasticsearch/
│     │  └─ book-index-settings.json
│     └─ lua/
│        └─ enforce-session-limit.lua
├─ docker-compose.yml
├─ Dockerfile
├─ pom.xml
└─ README.md
```

---

## Contributing

### Development Workflow
1. Fork the repository
2. Create feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m "feat: add new feature"`
4. Push to branch: `git push origin feature/your-feature`
5. Create Pull Request

### Commit Convention
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `refactor:` Code refactoring
- `test:` Adding tests
- `chore:` Maintenance tasks

---

## License

MIT License - See LICENSE file for details

---

## Contact & Resources

- **GitHub**: [@LouisVie61](https://github.com/LouisVie61)
- **Project**: [ReliefConnectForum](https://github.com/LouisVie61/ReliefConnectForum)
- **Issues**: [GitHub Issues](https://github.com/LouisVie61/ReliefConnectForum/issues)

---

## Acknowledgments

- Spring Boot Team for excellent framework
- Elasticsearch for powerful search capabilities
- Redis for blazing-fast caching
- PostgreSQL for reliable data storage
- OpenAPI community for Swagger UI

---

**Last Updated**: December 8, 2024  
**Version**: 3.0.0  
**Status**: ✅ Production Ready (with production hardening recommendations)

---

## AI-Worker Monolithic

### Overview
The AI-Worker is a separate Monolithic responsible for **intelligent post classification** using Natural Language Processing (NLP). It runs independently from the main Spring Boot application and communicates via Redis job queue.

### Architecture

**Communication Pattern:**
- **Async Queue-Based**: Spring Boot pushes jobs to Redis, AI-Worker pulls and processes
- **Decoupled**: AI-Worker can scale independently
- **Fault Tolerant**: Jobs remain in queue if worker is down
- **Bi-directional**: Worker calls back to Spring Boot API to update results

### Technology Stack (AI-Worker)

**Option 1: Python (Recommended)**
- **Framework**: Flask or FastAPI
- **NLP Library**: transformers (Hugging Face), spaCy, or scikit-learn
- **Redis Client**: redis-py
- **HTTP Client**: requests or httpx
- **Model**: PhoBERT (Vietnamese BERT) or multilingual BERT

**Option 2: Node.js**
- **Framework**: Express.js
- **NLP Library**: natural, compromise, or TensorFlow.js
- **Redis Client**: ioredis
- **HTTP Client**: axios

### Job Processing Flow

**1. Job Submission (Spring Boot)**
```java
@Service
public class AIJobService {
    private static final String AI_JOB_QUEUE = "ai_jobs_queue";
    
    @Async("aiJobExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        Map<String, Object> jobPayload = Map.of(
            "postId", event.getPostId().toString(),
            "timestamp", System.currentTimeMillis()
        );
        
        redisTemplate.opsForList().rightPush(AI_JOB_QUEUE, jobPayload);
        logger.info("AI job submitted for post: {}", event.getPostId());
    }
}
```

**2. Job Consumption (AI-Worker - Python Example)**
```python
import redis
import requests
import json
from transformers import pipeline

# Initialize
redis_client = redis.Redis(host='localhost', port=6380, db=0)
classifier = pipeline("text-classification", model="vinai/phobert-base")

QUEUE_NAME = "ai_jobs_queue"
API_BASE_URL = "http://localhost:8080/api/posts"
API_TOKEN = "your-service-account-token"

def classify_post(title, content):
    """Classify post into RESCUE, FUNDRAISE, or UPDATE"""
    text = f"{title}. {content}"
    
    # Keywords-based classification
    rescue_keywords = ['cứu', 'khẩn cấp', 'urgent', 'trapped', 'emergency']
    fundraise_keywords = ['quyên góp', 'donate', 'donation', 'fundraise', 'gây quỹ']
    
    text_lower = text.lower()
    
    if any(keyword in text_lower for keyword in rescue_keywords):
        return "RESCUE"
    elif any(keyword in text_lower for keyword in fundraise_keywords):
        return "FUNDRAISE"
    else:
        return "UPDATE"

def process_job(job_data):
    """Process a single AI job"""
    post_id = job_data['postId']
    
    # Fetch post details from API
    response = requests.get(
        f"{API_BASE_URL}/{post_id}",
        headers={"Authorization": f"Bearer {API_TOKEN}"}
    )
    
    if response.status_code != 200:
        print(f"Failed to fetch post {post_id}")
        return
    
    post = response.json()
    
    # Classify
    predicted_type = classify_post(post['title'], post['content'])
    
    # Update post via API
    update_response = requests.put(
        f"{API_BASE_URL}/{post_id}",
        headers={
            "Authorization": f"Bearer {API_TOKEN}",
            "Content-Type": "application/json"
        },
        json={"postType": predicted_type}
    )
    
    if update_response.status_code == 200:
        print(f"✓ Post {post_id} classified as {predicted_type}")
    else:
        print(f"✗ Failed to update post {post_id}")

def main():
    """Main worker loop"""
    print("AI-Worker started. Waiting for jobs...")
    
    while True:
        try:
            # BLPOP blocks until job is available (timeout: 5 seconds)
            result = redis_client.blpop(QUEUE_NAME, timeout=5)
            
            if result:
                _, job_bytes = result
                job_data = json.loads(job_bytes)
                print(f"Processing job: {job_data}")
                process_job(job_data)
                
        except Exception as e:
            print(f"Error processing job: {e}")
            continue

if __name__ == "__main__":
    main()
```

### Redis Queue Structure

**Queue Name:** `ai_jobs_queue`

**Job Payload Format:**
```json
{
  "postId": "a1b2c3d4-5678-90ab-cdef-1234567890ab",
  "timestamp": 1733654400000
}
```

**Queue Operations:**
- **Producer (Spring Boot)**: `RPUSH ai_jobs_queue {job_data}`
- **Consumer (AI-Worker)**: `BLPOP ai_jobs_queue 5` (blocking pop with 5s timeout)
- **Monitor Queue Length**: `LLEN ai_jobs_queue`

### Classification Algorithm

**Approach 1: Keyword-Based (Simple)**
```python
CLASSIFICATION_RULES = {
    "RESCUE": [
        "cứu", "khẩn cấp", "mắc kẹt", "nguy hiểm", 
        "urgent", "emergency", "trapped", "help", "rescue"
    ],
    "FUNDRAISE": [
        "quyên góp", "gây quỹ", "ủng hộ", "donate", 
        "donation", "fundraise", "contribute", "support"
    ],
    "UPDATE": [
        "cập nhật", "thông tin", "tình hình", 
        "update", "status", "information", "news"
    ]
}

def classify(text):
    text_lower = text.lower()
    scores = {category: 0 for category in CLASSIFICATION_RULES}
    
    for category, keywords in CLASSIFICATION_RULES.items():
        for keyword in keywords:
            if keyword in text_lower:
                scores[category] += 1
    
    return max(scores, key=scores.get) if max(scores.values()) > 0 else "UPDATE"
```

**Approach 2: ML-Based (Advanced)**
```python
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

# Load PhoBERT model fine-tuned on disaster classification
model_name = "vinai/phobert-base"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(
    "path/to/fine-tuned-model",
    num_labels=3  # RESCUE, FUNDRAISE, UPDATE
)

def classify_ml(text):
    inputs = tokenizer(text, return_tensors="pt", truncation=True, max_length=256)
    outputs = model(**inputs)
    predictions = torch.nn.functional.softmax(outputs.logits, dim=-1)
    
    label_map = {0: "RESCUE", 1: "FUNDRAISE", 2: "UPDATE"}
    predicted_class = torch.argmax(predictions).item()
    confidence = predictions[0][predicted_class].item()
    
    return label_map[predicted_class], confidence
```

### Deployment

**Docker Compose Integration:**
```yaml
services:
  # ...existing services...
  
  ai-worker:
    build:
      context: ./ai-worker
      dockerfile: Dockerfile
    container_name: relief-ai-worker
    environment:
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - API_BASE_URL=http://app:8080
      - API_TOKEN=${AI_WORKER_TOKEN}
      - LOG_LEVEL=INFO
    depends_on:
      - redis
      - app
    restart: unless-stopped
```

**AI-Worker Dockerfile (Python):**
```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["python", "worker.py"]
```

**requirements.txt:**
```txt
redis==5.0.1
requests==2.31.0
transformers==4.35.0
torch==2.1.0
pydantic==2.5.0
python-dotenv==1.0.0
```

### Monitoring & Observability

**Health Check Endpoint (Optional):**
```python
from flask import Flask, jsonify

health_app = Flask(__name__)

@health_app.route('/health')
def health():
    return jsonify({
        "status": "healthy",
        "redis_connected": redis_client.ping(),
        "queue_length": redis_client.llen(QUEUE_NAME),
        "processed_jobs": processed_count
    })

# Run in separate thread
if __name__ == "__main__":
    health_app.run(host='0.0.0.0', port=5000)
```

**Metrics to Track:**
- Jobs processed per minute
- Average processing time
- Classification accuracy
- Queue backlog size
- Failed job rate

**Redis Commands for Monitoring:**
```bash
# Check queue length
redis-cli -p 6380 LLEN ai_jobs_queue

# View jobs without removing
redis-cli -p 6380 LRANGE ai_jobs_queue 0 10

# Clear queue (if needed)
redis-cli -p 6380 DEL ai_jobs_queue
```

### Scaling Strategies

**Horizontal Scaling:**
- Run multiple AI-Worker instances
- Each worker competes for jobs using `BLPOP` (atomic operation)
- No coordination needed - Redis handles distribution

**Example: 3 Workers**
```bash
docker-compose up -d --scale ai-worker=3
```

**Load Balancing:**
- Redis `BLPOP` naturally load balances across workers
- First available worker gets the next job
- No additional load balancer needed

### Error Handling

**Retry Logic:**
```python
MAX_RETRIES = 3

def process_job_with_retry(job_data):
    for attempt in range(MAX_RETRIES):
        try:
            process_job(job_data)
            return True
        except Exception as e:
            print(f"Attempt {attempt + 1} failed: {e}")
            if attempt == MAX_RETRIES - 1:
                # Move to dead letter queue
                redis_client.rpush("ai_jobs_dlq", json.dumps(job_data))
                return False
            time.sleep(2 ** attempt)  # Exponential backoff
```

**Dead Letter Queue:**
- Failed jobs → `ai_jobs_dlq`
- Manual review or retry later
- Prevents job loss

### Performance Optimization

**Batch Processing:**
```python
def process_batch(jobs):
    """Process multiple jobs at once for efficiency"""
    # Fetch all posts in one request
    post_ids = [job['postId'] for job in jobs]
    
    # Batch classify
    predictions = classifier([post['title'] + post['content'] for post in posts])
    
    # Batch update
    for post_id, prediction in zip(post_ids, predictions):
        update_post(post_id, prediction['label'])
```

**Caching:**
- Cache model in memory (don't reload per job)
- Cache API tokens
- Reuse HTTP connection pool

---

### Future Enhancements

🔮 **Roadmap:**
- Sentiment analysis (urgency detection)
- Location extraction (NER)
- Multilingual support (English, Vietnamese)
- Confidence scores for classifications
- Active learning (human feedback loop)
- Real-time model updates
- A/B testing different models

