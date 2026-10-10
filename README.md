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

Open `http://localhost:8080` to view the landing page. Inquiries are stored in `data/collected-data.db`; submitting the form does not send email notifications.

Edit `index.html`, `styles.css`, and `script.js` in the project root. Maven packages these files and `assets/` as Spring Boot static resources. Restart the app after editing to copy updated assets. Opening the HTML directly supports a visual preview, but form submissions require the running backend.

The page includes mobile navigation, expandable FAQs, scroll reveals with reduced-motion support, and form success/error states. Confirm service descriptions with the business before publication.

Existing backend review finding: `GET /api/collected-data` returns all inquiries without authentication. Protect or remove this endpoint before making the application public.

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
