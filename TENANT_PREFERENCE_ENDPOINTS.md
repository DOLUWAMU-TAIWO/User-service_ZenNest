# Tenant-Specific User Preference Endpoints

This document describes the addition of controller endpoints for tenant-specific user preference fields in the `UserController` class. These endpoints allow authenticated users to update their preferences such as search radius, price alerts, new listing alerts, visit reminders, auto-save searches, max budget, preferred property types, and preferred amenities.

## Endpoints Added

- `POST /api/users/set-search-radius` — Set the user's search radius
- `POST /api/users/set-price-alerts` — Enable/disable price alerts
- `POST /api/users/set-new-listing-alerts` — Enable/disable new listing alerts
- `POST /api/users/set-visit-reminders` — Enable/disable visit reminders
- `POST /api/users/set-auto-save-searches` — Enable/disable auto-save searches
- `POST /api/users/set-max-budget` — Set the user's max budget
- `POST /api/users/set-preferred-property-types` — Set preferred property types
- `POST /api/users/set-preferred-amenities` — Set preferred amenities

## Example Endpoint Implementation

Each endpoint expects a valid JWT in the `Authorization` header. Example for setting search radius:

```java
@PostMapping("/set-search-radius")
public ResponseEntity<?> setSearchRadius(@RequestParam("radius") Integer radius, HttpServletRequest httpRequest) {
    try {
        String jwt = httpRequest.getHeader("Authorization");
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
        }
        String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }
        userService.updateSearchRadius(user.getId(), radius);
        return ResponseEntity.ok(Map.of("message", "Search radius updated successfully.", "searchRadius", radius));
    } catch (ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
    }
}
```

Other endpoints follow a similar pattern, calling the appropriate `userService` method for each preference field.

## Service Layer

The following methods are used in the service layer (`UserService.java`):
- `updateSearchRadius(UUID userId, Integer searchRadius)`
- `updatePriceAlerts(UUID userId, Boolean priceAlerts)`
- `updateNewListingAlerts(UUID userId, Boolean newListingAlerts)`
- `updateVisitReminders(UUID userId, Boolean visitReminders)`
- `updateAutoSaveSearches(UUID userId, Boolean autoSaveSearches)`
- `updateMaxBudget(UUID userId, Double maxBudget)`
- `updatePreferredPropertyTypes(UUID userId, List<String> preferredPropertyTypes)`
- `updatePreferredAmenities(UUID userId, List<String> preferredAmenities)`

## Usage

- All endpoints require authentication via JWT.
- For boolean fields, use `enabled=true|false` as a request parameter.
- For list fields, send a JSON body with the appropriate key (e.g., `{ "propertyTypes": ["apartment", "house"] }`).

## Example Request (cURL)

```
curl -X POST "https://yourdomain/api/users/set-search-radius?radius=10" \
  -H "Authorization: Bearer <token>"
```

## Notes
- All endpoints return a JSON response with a success message and the updated value, or an error message if the operation fails.
- The implementation assumes the existence of the corresponding service methods and that the `User` entity has the relevant fields.

