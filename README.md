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

Before posting, fetch a human verification challenge:

`GET /api/human-verification/challenge`

Request body:

```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "555-1234",
  "message": "I need help with home care services.",
  "humanVerificationToken": "<token from challenge endpoint>",
  "humanVerificationAnswer": "9"
}
```

Submissions require a valid human-verification response, and email validity is checked before data is stored. Response includes whether a confirmation email was sent.

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
- `HUMAN_VERIFICATION_SECRET`

If SMTP is unavailable, submissions are still saved and the API reports that confirmation email sending failed.

## Live deployment safety notes

- Set strong values for `ADMIN_API_KEY` and `HUMAN_VERIFICATION_SECRET` before exposing the site publicly.
- API error responses are normalized to JSON messages so malformed/invalid requests do not surface unstable server errors to clients.
