# Profile Settings API Documentation

This document describes all available endpoints and fields for managing user profile settings in the Qorelabs User Service. These endpoints allow users to update their profile preferences, notification settings, visitation and booking options, and more.

---

## Authentication
All endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

---

## REST Endpoints

### 1. Get User Profile Settings
**GET** `/api/users/user-details`

Returns all user profile and settings fields.

---

### 2. Set Visit Duration
**POST** `/api/users/set-visit-duration?duration=THIRTY_MINUTES`
- `duration` (required): One of: `FIFTEEN_MINUTES`, `THIRTY_MINUTES`, `FORTY_FIVE_MINUTES`, `ONE_HOUR`, `NINETY_MINUTES`, `TWO_HOURS`

---

### 3. Set Auto-Accept Visitation
**POST** `/api/users/set-auto-accept-visitation?enabled=true`
- `enabled` (required): `true` or `false`

---

### 4. Set Auto-Accept Booking
**POST** `/api/users/set-auto-accept-booking?enabled=true`
- `enabled` (required): `true` or `false`

---

### 5. Set Notification Preferences
**POST** `/api/users/set-notification-preferences?emailEnabled=true&smsEnabled=false&pushEnabled=true`
- `emailEnabled` (optional): `true` or `false`
- `smsEnabled` (optional): `true` or `false`
- `pushEnabled` (optional): `true` or `false`

---

### 6. Set Buffer Time
**POST** `/api/users/set-buffer-time?hours=24`
- `hours` (required): Integer between 0 and 72

---

### 7. Update Subscription
**PATCH** `/api/users/{id}/subscription`
**Body:**
```json
{
  "plan": "Premium",
  "active": true
}
```

---

### 8. Mark Profile Complete
**PATCH** `/api/users/{id}/profile-complete`

---

### 9. Mark Onboarding Complete
**PATCH** `/api/users/{id}/onboarding-complete`

---

### 10. Add/Remove Favourites
**POST** `/api/users/favourites/add`
```json
{
  "userId": "user-uuid",
  "listingId": "listing-uuid"
}
```
**POST** `/api/users/favourites/remove`
```json
{
  "userId": "user-uuid",
  "listingId": "listing-uuid"
}
```

---

### 11. Change Password
**POST** `/api/users/change-password`
```json
{
  "oldPassword": "...",
  "newPassword": "..."
}
```

---

### 12. Set FCM Device Token
Include the FCM device token in the user profile update or registration payload as `fcmDeviceToken`.

---

## GraphQL Mutations for Profile Settings

You can also update user profile settings using GraphQL mutations. Example fields you can change:
- `firstName`
- `lastName`
- `username`
- `phoneNumber`
- `email`
- `password`
- `profession`
- `city`
- `country`
- `profileDescription`
- `profilePicture`
- `favourites`
- `profileCompleted`
- `onboardingCompleted`
- `subscriptionPlan`
- `subscriptionActive`
- `openVisitations`
- `paymentVerified`
- `totalEarnings`
- `completedFeatures`

**Example Mutation:**
```graphql
mutation {
  updateUser(
    id: "user-uuid"
    firstName: "Jane"
    lastName: "Smith"
    city: "Lagos"
    email: "jane.smith@example.com"
    profileCompleted: true
    onboardingCompleted: true
    subscriptionPlan: "Premium"
    subscriptionActive: true
    openVisitations: true
    paymentVerified: true
  ) {
    id
    firstName
    lastName
    city
    email
    profileCompleted
    onboardingCompleted
    subscriptionPlan
    subscriptionActive
    openVisitations
    paymentVerified
  }
}
```

---

## Settings Fields Reference

- **visitDuration**: User's preferred visit duration for property viewings.
- **autoAcceptBooking**: Automatically accept booking requests.
- **autoAcceptVisitation**: Automatically accept visitation requests.
- **emailNotificationsEnabled**: Receive notifications via email.
- **smsNotificationsEnabled**: Receive notifications via SMS.
- **pushNotificationsEnabled**: Receive push notifications (FCM).
- **bufferTimeHours**: Buffer time (in hours) between bookings/visitations.
- **fcmDeviceToken**: Device token for push notifications.
- **profileCompleted**: Whether the user has completed their profile.
- **onboardingCompleted**: Whether the user has completed onboarding.
- **subscriptionPlan**: User's current subscription plan.
- **subscriptionActive**: Whether the subscription is active.
- **openVisitations**: Whether the user is open to visitations.
- **paymentVerified**: Whether the user's payment is verified.
- **firstName, lastName, username, city, country, email, phoneNumber, profession, profileDescription, profilePicture**: Basic profile fields.

---

## Notes
- All endpoints require authentication.
- All settings changes are immediately reflected in the user profile.
- Use the `/user-details` endpoint or GraphQL queries to fetch the latest settings for the current user.

---

For more details on any endpoint or field, see the backend code or contact the backend team.

