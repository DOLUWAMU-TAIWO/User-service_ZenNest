# Get User Payout Info API

## Endpoint

```
GET /api/users/{id}/payout-info
```

## Description

Checks if a user has payout information stored in the system. Returns the payout status and account holder name if available.

## Authentication

- Requires a valid JWT token in the `Authorization` header.
- Only authenticated users can access this endpoint.

## Request

- **Path Parameter:**  
  `id` (UUID) — The user's unique identifier.

- **Header:**  
  `Authorization: Bearer <JWT_TOKEN>`

## Response

- **200 OK**  
  ```json
  {
    "success": true,
    "accountName": "John Doe"
  }
  ```
  Indicates payout info is present for the user.

- **404 Not Found**  
  ```json
  {
    "success": false,
    "accountName": null
  }
  ```
  Indicates payout info is not present for the user.

## Example

```bash
curl -X GET "https://yourdomain.com/api/users/{id}/payout-info" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

For further details or enhancements, contact the backend team.

