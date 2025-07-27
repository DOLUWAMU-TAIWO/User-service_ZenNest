# Newsletter API Integration Guide

This guide explains how to integrate your frontend with the backend newsletter feature and send emails to subscribers.

## Endpoints

### 1. Subscribe an Email
- **Endpoint:** `POST /newsletter/subscribe`
- **Query Parameter:** `email` (string, required)
- **Example Request:**
  ```http
  POST http://<API_BASE_URL>/newsletter/subscribe?email=user@example.com
  ```
- **Success Response:**
  ```json
  "Subscribed successfully."
  ```
- **Error Response:**
  ```json
  "Email already subscribed."
  ```

### 2. Send Newsletter to All Subscribers
- **Endpoint:** `POST /newsletter/send`
- **Query Parameters:**
  - `subject` (string, required)
  - `content` (string, required)
- **Example Request:**
  ```http
  POST http://<API_BASE_URL>/newsletter/send?subject=Hello&content=Welcome+to+our+newsletter!
  ```
- **Success Response:**
  ```json
  "Newsletter sent to N subscribers."
  ```

## How to Use (Frontend)

1. **Subscribe Users:**
   - Collect user email addresses via a form.
   - Send a POST request to `/newsletter/subscribe` with the email as a query parameter.
   - Show success or error message based on the response.

2. **Send Newsletter:**
   - Provide a form for admins to enter the subject and content.
   - Send a POST request to `/newsletter/send` with subject and content as query parameters.
   - Show the result (number of emails sent).

## Example (JavaScript, fetch)
```js
// Subscribe an email
fetch('http://localhost:7500/newsletter/subscribe?email=user@example.com', {
  method: 'POST'
})
  .then(res => res.text())
  .then(alert);

// Send newsletter
fetch('http://localhost:7500/newsletter/send?subject=Hello&content=Welcome+to+our+newsletter!', {
  method: 'POST'
})
  .then(res => res.text())
  .then(alert);
```

## Notes
- All requests are POST.
- Query parameters are used for simplicity.
- The backend will send emails using the configured email service.
- Make sure to handle errors and display appropriate messages to users.

## API Base URL
Replace `<API_BASE_URL>` with your backend URL, e.g. `http://localhost:7500` for local development.

