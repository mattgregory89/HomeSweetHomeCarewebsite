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

The page includes mobile navigation, Google reCAPTCHA v2, a keyword-based FAQ assistant, expandable FAQs, scroll reveals with reduced-motion support, and form success/error states. Confirm service descriptions with the business before publication.

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

The browser loads the public site key from `GET /api/recaptcha/config` and displays the reCAPTCHA v2 checkbox. The backend verifies the resulting token with Google before saving the inquiry.

Request body:

```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "555-1234",
  "message": "I need help with home care services.",
  "recaptchaToken": "<token from the completed reCAPTCHA widget>"
}
```

Submissions require a valid reCAPTCHA response, and email validity is checked before data is stored. Response includes whether a confirmation email was sent.

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
- `RECAPTCHA_SITE_KEY`
- `RECAPTCHA_SECRET_KEY`

If SMTP is unavailable, submissions are still saved and the API reports that confirmation email sending failed.

## Live deployment safety notes

- Set a strong `ADMIN_API_KEY` and configure reCAPTCHA keys before exposing the site publicly.
- API error responses are normalized to JSON messages so malformed/invalid requests do not surface unstable server errors to clients.

## Validate before pushing

```bash
mvn clean verify
```

Tests use a separate in-memory SQLite database and cover static resources, verified submissions, email failure, invalid requests, and admin access. Use Java 17 for the verified build. The Care Assistant uses local keyword matching; it does not call an AI service.

## Set up reCAPTCHA

Register a **reCAPTCHA v2 “I'm not a robot” checkbox** site in the [Google reCAPTCHA console](https://www.google.com/recaptcha/admin/create). Add your production hostname and `localhost` for local development. Use keys compatible with the [SiteVerify API](https://developers.google.com/recaptcha/docs/verify), rather than an Enterprise-only integration.

Configure `RECAPTCHA_SITE_KEY` and `RECAPTCHA_SECRET_KEY` in your server environment, then restart the app. The site key is public; keep the secret key on the server and out of Git. The integration does not allow submissions when keys are missing or verification is unavailable.

For local development, set both environment variables in your IDE run configuration or terminal, then run `mvn spring-boot:run` and open `http://localhost:8080`. Live Server, GitHub Pages, and opening `index.html` directly do not run the Java API, so the contact form cannot submit through those previews. A Google script blocker or missing internet access can also prevent the checkbox from loading.

Tests simulate Google verification responses; they do not contact Google or require real keys. Live checkbox testing requires your configured keys.

## Docker

Create a local environment file from the template:

```bash
cp .env.example .env
```

Replace the example values, especially `ADMIN_API_KEY`, reCAPTCHA keys, and SMTP credentials. Then build and start the application:

```bash
docker compose up --build
```

Open `http://localhost:8080`. Docker Compose persists the SQLite database in the `hshc-data` volume.

Build and run without Compose:

```bash
docker build -t homesweethomecare:latest .
docker run --env-file .env -p 8080:8080 -v hshc-data:/app/data homesweethomecare:latest
```

## AWS deployment

The image can be deployed to Amazon ECS/Fargate or EC2. Define `ADMIN_API_KEY`, `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET_KEY`, and SMTP variables in AWS Secrets Manager or ECS task environment variables.

Push the image to Amazon ECR:

```bash
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com
aws ecr create-repository --repository-name homesweethomecare
docker tag homesweethomecare:latest <account-id>.dkr.ecr.<region>.amazonaws.com/homesweethomecare:latest
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/homesweethomecare:latest
```

Configure ECS or App Runner with container port `8080`, environment `PORT=8080`, and `DB_PATH=/app/data/collected-data.db`. Mount persistent storage or use a managed database for production; SQLite data in a container filesystem is otherwise ephemeral. Amazon EFS can persist SQLite for ECS tasks, but concurrent writers and load-balanced replicas must be avoided. An external relational database is safer for horizontal scaling.

Health check: `GET /api/recaptcha/config` on port `8080`.
