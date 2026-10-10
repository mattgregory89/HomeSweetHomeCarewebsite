# HomeSweetHomeCarewebsite

Spring Boot website and REST API backend with SQLite storage for contact submissions.

## Requirements

- Java 17+
- Maven 3.9+

## Run locally

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080` and stores data in `data/collected-data.db`.

## Website pages

- `/` - landing page + contact form
- `/scheduling.html` - scheduling information
- `/services.html` - services offered
- `/faq.html` - frequently asked questions
- `/admin.html` - admin view for contact submissions

## API Endpoints

### Submit contact form

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

Response includes whether a confirmation email was sent.

### List contact submissions (admin)

`GET /api/admin/collected-data`

Required header: `X-Admin-Key`

## Configuration

Set these environment variables for production:

- `ADMIN_API_KEY` (required for admin page access)
- `MAIL_FROM_ADDRESS`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`

If SMTP is unavailable, submissions are still saved and the API reports that confirmation email sending failed.
