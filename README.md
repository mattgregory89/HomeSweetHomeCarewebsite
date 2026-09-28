# HomeSweetHomeCarewebsite

Spring Boot REST API backend with SQLite storage for collected website data.

## Requirements

- Java 17+
- Maven 3.9+

## Run locally

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080` and stores data in `data/collected-data.db`.

## Endpoints

### Create collected data

`POST /api/collected-data`

Request body:

```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "555-1234",
  "message": "I need help with home care services."
}
```

### List collected data

`GET /api/collected-data`
