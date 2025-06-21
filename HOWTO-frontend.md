# User Service API Guide

This document describes how to interact with the User Service API for frontend developers. It covers authentication, endpoints, request/response formats, and best practices for accessing and modifying user resources.

---

## Authentication

- **API Key:** Most endpoints require an `X-API-KEY` header. Obtain this key from your backend admin.
- **JWT Bearer Token:** For authenticated user actions, include an `Authorization: Bearer <token>` header.

---

## Common Headers

```
X-API-KEY: <your-api-key>
Authorization: Bearer <jwt-token>   # (if required)
Content-Type: application/json
```

---

## User Endpoints

### Register a User
- **POST** `/api/users/register`
- **Body:**
```json
{
  "email": "user@example.com",
  "password": "yourPassword",
  "firstName": "John",
  "lastName": "Doe",
  ...
}
```
- **Response:** User object or status

### Login
- **POST** `/api/users/login`
- **Body:**
```json
{
  "email": "user@example.com",
  "password": "yourPassword"
}
```
- **Response:** `{ "accessToken": "...", "refreshToken": "..." }`

### Get User Details
- **GET** `/api/users/{id}`
- **Headers:** `Authorization: Bearer <token>`
- **Response:** User object

---

## Payout Info

### Resolve and Save Payout Info
- **POST** `/api/users/{id}/payout-info`
- **Headers:** `X-API-KEY`
- **Body:**
```json
{
  "accountNumber": "0123456789",
  "bankCode": "058",
  "bvn": "optional"
}
```
- **Response:**
```json
{
  "success": true,
  "accountName": "DOLUWAMU KUYE"
}
```

---

## Onboarding Features

### Mark Feature Completed
- **POST** `/api/users/{id}/onboarding/{feature}`
- **Headers:** `X-API-KEY`
- **Response:**
```json
["VERIFY_EMAIL", "COMPLETE_PROFILE", ...]
```

### Get Completed Features
- **GET** `/api/users/{id}/onboarding`
- **Headers:** `X-API-KEY`
- **Response:**
```json
["VERIFY_EMAIL", "COMPLETE_PROFILE", ...]
```

---

## Error Handling
- **404 Not Found:** User/resource does not exist.
- **400 Bad Request:** Invalid input or feature name.
- **401 Unauthorized:** Missing or invalid API key or token.
- **502 Bad Gateway:** Upstream service error (e.g., payment service).

---

## Best Practices
- Always include required headers.
- Validate user input before sending requests.
- Handle error responses gracefully in the UI.
- Use the latest API documentation for new features.

---

## More
- For GraphQL, see `/graphql` endpoint and use the GraphiQL UI if enabled.
- For additional endpoints (search, update, etc.), see the OpenAPI/Swagger docs or ask the backend team.

---

*Contact backend team for API keys, tokens, or further questions.*

