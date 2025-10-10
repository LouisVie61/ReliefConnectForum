# Relief Connect Forum — Version 1 (Monolith)

Monolithic Spring Boot service for publishing and managing disaster-relief posts (help requests and fundraising). Version 1 focuses on simple CRUD, a single service, and PostgreSQL persistence.

## Tech stack
- Spring Boot, Spring Web, Spring Data JPA
- PostgreSQL (UUID PK, `ddl-auto: update`)
- springdoc-openapi for Swagger UI
- Maven build
- IntelliJ IDEA
- No HTTP session persistence (Spring Session disabled/removed)

## Architecture (v1)
- Layered MVC:
  - `controller`: REST endpoints under `/api/posts`
  - `service`: thin domain operations
  - `repository`: `JpaRepository<Post, UUID>`
  - `entity`: `Post` JPA entity, `PostType` enum (`HELP`, `FUNDRAISE`)
- Cross-cutting:
  - Global CORS for `/api/**` in `WebConfig`
  - OpenAPI config in `SwaggerConfig`
- Database:
  - Table `posts` with columns: `id`(UUID), `title`, `description`, `post_type`, `location`, `contact_name`, `contact_phone`, `target_amount`, `current_amount`, `created_at`
  - `@PrePersist` sets `created_at` and `current_amount = 0` when missing
  - Seed data in `data.sql`

## Run locally (Windows)
Prerequisites: JDK 17+, Maven 3.9+, PostgreSQL with a database `reliefV1`.

1) Configure dev profile in `src/main/resources/application-dev.yml` (defaults: user `postgres`, password `123456`).
2) Build and run:
```bash
mvn clean package
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# or
# java -jar target/<your-jar>.jar --spring.profiles.active=dev
```
Open:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API docs: http://localhost:8080/v3/api-docs

## API summary
Base path: `/api/posts`

- GET `/api/posts` — list all posts.
- GET `/api/posts/{id}` — fetch by UUID.
- POST `/api/posts` — create a post.
- DELETE `/api/posts/{id}` — delete by UUID.

Example create payload:
```json
{
  "title": "Need boat evacuation",
  "description": "Elderly couple trapped, water rising.",
  "postType": "HELP",
  "location": "Ward 5, Riverside",
  "contactName": "Lan",
  "contactPhone": "0900000001",
  "targetAmount": 10000000
}
```
Notes:
- Do not send `id`, `currentAmount`, or `createdAt` — they are read-only.
- Enum `postType` accepts `HELP` or `FUNDRAISE`.

## Data seeding
`src/main/resources/data.sql` inserts two sample rows at startup for quick testing.

## Release v1 tag
From the repository root:
```bash
git checkout master   # or branch 'version_1' if you tag there
git pull
git tag -a v1.0.0 -m "chore(release): v1.0.0"
git push origin v1.0.0
```
Optionally publish a GitHub Release from the pushed tag.

## Limitations in v1
- No authentication/authorization.
- No validation/pagination/filtering.
- Open CORS `*` for `/api/**` (review before production).
- Monolithic deployment; no messaging or background jobs.

