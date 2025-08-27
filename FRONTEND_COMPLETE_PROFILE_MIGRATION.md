# 🚀 Frontend Migration Guide: Complete Profile with Magic Links

## 📋 Overview

We've upgraded the profile completion system with a new dedicated `completeProfile` GraphQL mutation that includes **magic link functionality**. This replaces the previous `updateUser` mutation and provides users with seamless access to role-specific features via email.

## 🔄 What Changed

### ❌ Old Implementation
- Used generic `updateUser` mutation
- Basic success message without email mention
- Users had to manually navigate to dashboard after completion

### ✅ New Implementation  
- Uses dedicated `completeProfile` mutation
- **Role-based magic links** sent via email:
  - **Tenants**: Direct link to property search (`https://zennest.africa/search-results?token=<jwt>`)
  - **Landlords**: Direct link to create listing (`https://zennest.africa/landlord-dashboard/listings/create?token=<jwt>`)
- Enhanced success messaging mentioning email
- **30-minute secure JWT tokens** with user ID, email, and role
- **Fallback safety**: If magic links fail, regular emails are sent

---

## 🔧 Required Changes

### 1. **Update GraphQL Mutation**

Replace your existing `updateUser` mutation with this new one:

```javascript
const mutation = gql`
  mutation CompleteProfile(
    $id: ID!, 
    $firstName: String!, 
    $lastName: String!, 
    $email: String!, 
    $phoneNumber: String!,
    $role: Role!, 
    $city: String!, 
    $intention: UserIntention!,
    $profileDescription: String!, 
    $profilePicture: String,
    $profession: String, 
    $dateOfBirth: String!,
    $openVisitations: Boolean,
    $autoAcceptBooking: Boolean,
    $visitDuration: VisitDuration,
    $bufferTimeHours: Int
  ) {
    completeProfile(
      id: $id,
      firstName: $firstName,
      lastName: $lastName,
      email: $email,
      phoneNumber: $phoneNumber,
      role: $role,
      city: $city,
      intention: $intention,
      profileDescription: $profileDescription,
      profilePicture: $profilePicture,
      profession: $profession,
      dateOfBirth: $dateOfBirth,
      openVisitations: $openVisitations,
      autoAcceptBooking: $autoAcceptBooking,
      visitDuration: $visitDuration,
      bufferTimeHours: $bufferTimeHours
    ) {
      id
      firstName
      lastName
      email
      phoneNumber
      profession
      dateOfBirth
      openVisitations
      role
      city
      intention
      profileDescription
      profilePicture
      profileCompleted
      autoAcceptBooking
      visitDuration
      bufferTimeHours
    }
  }
`;
```

### 2. **Update Variables Object**

```javascript
const variables = {
  id: user.id,
  firstName,
  lastName,
  email,
  phoneNumber,
  role,
  city,
  intention,
  profileDescription,
  profilePicture: profilePicture || null,
  profession: profession || null,
  dateOfBirth: dateOfBirth || null,
  // Landlord-specific fields (only include if role is LANDLORD)
  ...(role === 'LANDLORD' && {
    openVisitations,
    autoAcceptBooking,
    visitDuration: visitDuration ? convertToEnumFormat(visitDuration) : null,
    bufferTimeHours: bufferTimeHours ? parseInt(bufferTimeHours) : null
  })
};

// Helper function for visit duration conversion
const convertToEnumFormat = (duration) => {
  const mapping = {
    "15": "FIFTEEN_MINUTES",
    "30": "THIRTY_MINUTES", 
    "45": "FORTY_FIVE_MINUTES",
    "60": "ONE_HOUR",
    "90": "NINETY_MINUTES",
    "120": "TWO_HOURS"
  };
  return mapping[duration] || "THIRTY_MINUTES";
};
```

### 3. **Update handleSubmit Function**

Replace your existing `handleSubmit` with this enhanced version:

```javascript
const handleSubmit = async (e) => {
  e.preventDefault();
  if (!validateStep()) return;
  setError("");
  if (!role) {
    setError("Invalid intention selected; unable to determine role.");
    return;
  }
  setLoading(true);
  
  try {
    console.log('Submitting profile completion with variables:', variables);
    const token = localStorage.getItem('accessToken');
    const result = await request(
      endpoint,
      mutation,
      variables,
      { Authorization: `Bearer ${token}` }
    );
    console.log('Profile completion mutation result:', result);
    
    // ✅ NEW: Enhanced success message mentioning email magic link
    toast.success("Profile completed successfully! 🎉 Check your email for a magic link to get started immediately.");
    
    // Update context with new profile data
    const updatedUser = {
      ...user,
      ...result.completeProfile,
      profileCompleted: true
    };
    setUser(updatedUser);
    
    // Navigate based on role
    navigate(updatedUser.role === 'LANDLORD' ? '/landlord-dashboard' : '/tenant-dashboard');
    
  } catch (e) {
    console.error('Profile completion error:', e);
    
    // Handle GraphQL errors properly with sanitized user messages
    let userErrorMessage = "Failed to complete profile. Please try again.";
    
    if (e.response?.errors?.length > 0) {
      const graphqlErrors = e.response.errors.map(err => err.message).join('. ');
      const errorText = graphqlErrors.toLowerCase();
      
      // Handle specific error types with user-friendly messages
      if (errorText.includes('email') && errorText.includes('already in use')) {
        userErrorMessage = 'This email address is already registered. Please use a different email or try logging in.';
      } else if (errorText.includes('phone') && errorText.includes('already in use')) {
        userErrorMessage = 'This phone number is already registered. Please use a different number or try logging in.';
      } else if (errorText.includes('first name') && errorText.includes('invalid')) {
        userErrorMessage = 'Please enter a valid first name (letters only, 2-50 characters).';
      } else if (errorText.includes('last name') && errorText.includes('invalid')) {
        userErrorMessage = 'Please enter a valid last name (letters only, 2-50 characters).';
      } else if (errorText.includes('city') && errorText.includes('invalid')) {
        userErrorMessage = 'Please enter a valid city name.';
      } else if (errorText.includes('profession') && errorText.includes('invalid')) {
        userErrorMessage = 'Please enter a valid profession.';
      } else if (errorText.includes('description') && errorText.includes('too short')) {
        userErrorMessage = 'Profile description must be at least 10 characters long.';
      } else if (errorText.includes('must be at least 16 years old')) {
        userErrorMessage = 'You must be at least 16 years old to create an account.';
      } else if (errorText.includes('date of birth') && errorText.includes('invalid')) {
        userErrorMessage = 'Please enter a valid date of birth.';
      } else if (errorText.includes('profile picture') && errorText.includes('invalid')) {
        userErrorMessage = 'There was an issue with your profile picture. Please try uploading again.';
      } else if (errorText.includes('validation') || errorText.includes('invalid')) {
        userErrorMessage = 'Please check your information and try again.';
      } else if (errorText.includes('unauthorized') || errorText.includes('authentication')) {
        userErrorMessage = 'Session expired. Please refresh the page and try again.';
      } else if (errorText.includes('network') || errorText.includes('connection')) {
        userErrorMessage = 'Network error. Please check your connection and try again.';
      }
    } else if (e.response?.data?.errors) {
      userErrorMessage = "There was an issue with your submission. Please check your information and try again.";
    } else if (e.message && e.message.includes('NetworkError')) {
      userErrorMessage = "Network error. Please check your connection and try again.";
    } else if (e.message && e.message.includes('timeout')) {
      userErrorMessage = "Request timeout. Please try again.";
    }
    
    setError(userErrorMessage);
    toast.error(userErrorMessage);
  } finally {
    setLoading(false);
  }
};
```

---

## 📧 Magic Link Email Templates

### For Tenants
- **Subject**: "🎉 Welcome to Zennest, {firstName}! Start browsing properties now"
- **Magic Link**: Direct to property search with search filters
- **Features Highlighted**: Browse properties, book viewings, direct communication, save favorites

### For Landlords  
- **Subject**: "🏠 Welcome to Zennest, {firstName}! Create your first listing now"
- **Magic Link**: Direct to listing creation page
- **Features Highlighted**: List properties, reach quality tenants, manage viewings, secure payments
- **Motivation**: Shows earning potential (₦2.5M average annually)

---

## 🔒 Security Features

- **JWT Tokens**: 30-minute expiration for security
- **User Claims**: Include user ID, email, role, and magic link type
- **Fallback Safety**: If magic link generation fails, regular congratulations emails are sent
- **Async Processing**: Email sending doesn't block the profile completion API response

---

## 🎯 Key Benefits for Users

1. **Immediate Action**: Users can take action directly from email
2. **No Login Required**: Magic links bypass login for 30 minutes
3. **Role-Specific Experience**: 
   - Tenants → Property search
   - Landlords → Listing creation
4. **Higher Engagement**: Direct path to value-added features
5. **Professional Experience**: Beautiful, branded email templates

---

## 🧪 Testing Checklist

- [ ] Profile completion with tenant role → Check email for property search magic link
- [ ] Profile completion with landlord role → Check email for listing creation magic link  
- [ ] Magic link functionality → Click link and verify direct navigation without login
- [ ] Magic link expiration → Test that links expire after 30 minutes
- [ ] Fallback testing → Verify regular emails sent if magic link generation fails
- [ ] Error handling → Test validation errors and user-friendly messages
- [ ] Success flow → Verify proper navigation to dashboards after completion

---

## 📞 Support

If you encounter any issues during migration:

1. **Backend logs** will show magic link generation status
2. **Email delivery** status is logged for monitoring  
3. **Fallback mechanisms** ensure users always receive congratulations emails
4. **Error messages** are user-friendly and actionable

---

## 🚀 Implementation Priority

**HIGH PRIORITY**: This migration enhances user experience significantly and should be deployed as soon as possible to maximize user engagement and conversion rates.

**Estimated Impact**: 
- 📈 Higher user activation rates
- 🎯 Better role-specific onboarding 
- 💌 Increased email engagement
- ⚡ Faster time-to-value for new users
