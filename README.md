# HomeSweetHomeCarewebsite

Responsive landing page and Spring Boot REST API with SQLite storage for care inquiries. The design uses the supplied Home Sweet Home Care logo and a navy, gold, sage, and cream palette.

## Requirements

- Java 17+
- Maven 3.9+

## Run locally

```bash
mkdir -p data
mvn spring-boot:run
```

Open `http://localhost:8080` to view the landing page. Inquiries are stored in `data/collected-data.db`. Confirmation emails are attempted using the configured SMTP server. If sending fails, the inquiry is still saved.

Edit `index.html`, `styles.css`, and `script.js` in the project root. Maven packages these files and `assets/` as Spring Boot static resources. Restart the app after editing to copy updated assets. The root `index.html` is the only homepage source; supporting pages live in `src/main/resources/static/`. Opening the HTML directly supports a visual preview, but form submissions require the running backend.

The page includes mobile navigation, human verification, a keyword-based FAQ assistant, expandable FAQs, scroll reveals with reduced-motion support, and form success/error states. Confirm service descriptions with the business before publication.

Submission lists are available only through `GET /api/admin/collected-data` with a configured admin key. The previous public listing endpoint has been removed.

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

- `ADMIN_API_KEY` (required for admin page access; access is disabled when unset)
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

## Validate before pushing

```bash
mvn clean verify
```

Tests use a separate in-memory SQLite database and cover static resources, verified submissions, email failure, invalid requests, and admin access. Use Java 17 for the verified build. The Care Assistant uses local keyword matching; it does not call an AI service.
