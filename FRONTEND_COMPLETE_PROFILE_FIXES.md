# Complete Profile Frontend Compatibility Fixes

## Overview
This document provides the exact changes needed to make your frontend `CompleteProfile` component fully compatible with your backend GraphQL schema and controller.

## Critical Issues Identified

1. **Missing Required Field**: Backend requires `dateOfBirth` as non-null (`String!`), but frontend validation doesn't enforce this consistently
2. **GraphQL Variable Handling**: Conditional landlord variables can cause undefined variable errors
3. **ID Type Conversion**: Need explicit string conversion for user ID
4. **Error Message Mapping**: Generic error handling doesn't cover backend-specific validation messages

---

## Required Code Changes

### 1. Update Field Validation (Step 3)

**File**: `CompleteProfile.jsx`
**Function**: `validateStep()`

Replace the existing step 3 validation with:

```javascript
if (step === 3) {
  // Profile description validation
  if (!profileDescription.trim()) {
    errors.push('Profile description is required.');
    toast.error('Please provide a short description about yourself.');
  } else if (profileDescription.trim().length < 10) {
    errors.push('Profile description must be at least 10 characters.');
    toast.error('Please provide a more detailed description (at least 10 characters).');
  }
  
  // Date of birth validation - CRITICAL: This is required by backend
  if (!dateOfBirth) {
    errors.push('Date of birth is required.');
    toast.error('Please enter your date of birth.');
  } else {
    const dob = new Date(dateOfBirth);
    const today = new Date();
    
    // Validate date format
    if (isNaN(dob.getTime())) {
      errors.push('Please enter a valid date of birth.');
      toast.error('Please enter a valid date of birth.');
    } else {
      const age = today.getFullYear() - dob.getFullYear();
      const monthDiff = today.getMonth() - dob.getMonth();
      
      // More accurate age calculation
      const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate()) ? age - 1 : age;
      
      if (actualAge < 16) {
        errors.push('You must be at least 16 years old.');
        toast.error('You must be at least 16 years old.');
      } else if (actualAge > 120) {
        errors.push('Please enter a valid date of birth.');
        toast.error('Please enter a valid date of birth.');
      }
    }
  }
  
  // Optional profession validation
  if (profession && profession.trim().length > 0 && profession.trim().length < 2) {
    errors.push('Profession must be at least 2 characters if provided.');
    toast.error('Profession must be at least 2 characters if provided.');
  }
  
  if (errors.length > 0) {
    setStepError(errors[0]);
    return false;
  }
}
```

### 2. Complete handleSubmit Function Replacement

**File**: `CompleteProfile.jsx`
**Function**: `handleSubmit()`

Replace the entire `handleSubmit` function with:

```javascript
const handleSubmit = async (e) => {
  e.preventDefault();
  if (!validateStep()) return;
  setError("");
  
  // Validate role
  if (!role) {
    setError("Invalid intention selected; unable to determine role.");
    return;
  }
  
  // Critical: Ensure dateOfBirth is provided (backend requirement)
  if (!dateOfBirth) {
    setError("Date of birth is required for profile completion.");
    toast.error("Please enter your date of birth.");
    return;
  }
  
  setLoading(true);
  try {
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
    
    // Prepare base variables that are always required
    const baseVariables = {
      id: user.id.toString(), // Explicit string conversion for GraphQL ID type
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: email.toLowerCase().trim(),
      phoneNumber: phoneNumber.trim(),
      role,
      city: city.trim(),
      intention,
      profileDescription: profileDescription.trim(),
      dateOfBirth: dateOfBirth, // Backend requires this to be non-null
      profilePicture: profilePicture || null,
      profession: profession?.trim() || null,
    };

    // Always include landlord-specific fields (null for tenants to avoid GraphQL errors)
    const landlordVariables = role === 'LANDLORD' ? {
      openVisitations: openVisitations,
      autoAcceptBooking: autoAcceptBooking,
      visitDuration: visitDuration ? convertToEnumFormat(visitDuration) : null,
      bufferTimeHours: bufferTimeHours ? parseInt(bufferTimeHours, 10) : null
    } : {
      openVisitations: null,
      autoAcceptBooking: null,
      visitDuration: null,
      bufferTimeHours: null
    };

    const variables = {
      ...baseVariables,
      ...landlordVariables
    };

    console.log('Submitting profile completion with variables:', variables);
    const token = localStorage.getItem('accessToken');
    const result = await request(
      endpoint,
      mutation,
      variables,
      { Authorization: `Bearer ${token}` }
    );
    console.log('Profile completion mutation result:', result);
    
    // Success message
    toast.success("Profile completed successfully! 🎉 Check your email for a magic link to get started immediately.");
    
    // Update context with new profile data
    const updatedUser = {
      ...user,
      ...result.completeProfile,
      profileCompleted: true
    };
    setUser(updatedUser);
    console.log('Role at routing:', updatedUser.role);
    
    // Direct navigation based on updated role
    if (result && result.completeProfile) {
      navigate(updatedUser.role === 'LANDLORD' ? '/landlord-dashboard' : '/tenant-dashboard');
    } else {
      setError('Unexpected response from server. Please try again.');
    }
  } catch (e) {
    console.error('Profile completion error:', e);
    
    // Enhanced error handling with backend-specific messages
    let userErrorMessage = "Failed to complete profile. Please try again.";
    
    if (e.response?.errors?.length > 0) {
      const graphqlErrors = e.response.errors.map(err => err.message).join('. ');
      const errorText = graphqlErrors.toLowerCase();
      
      // Map backend validation errors to user-friendly messages
      if (errorText.includes('email') && errorText.includes('already in use')) {
        userErrorMessage = 'This email address is already registered. Please use a different email or try logging in.';
      } else if (errorText.includes('phone') && errorText.includes('already in use')) {
        userErrorMessage = 'This phone number is already registered. Please use a different number or try logging in.';
      } else if (errorText.includes('first name') && (errorText.includes('invalid') || errorText.includes('required'))) {
        userErrorMessage = 'Please enter a valid first name (letters only, 2-50 characters).';
      } else if (errorText.includes('last name') && (errorText.includes('invalid') || errorText.includes('required'))) {
        userErrorMessage = 'Please enter a valid last name (letters only, 2-50 characters).';
      } else if (errorText.includes('city') && (errorText.includes('invalid') || errorText.includes('required'))) {
        userErrorMessage = 'Please enter a valid city name.';
      } else if (errorText.includes('profession') && errorText.includes('invalid')) {
        userErrorMessage = 'Please enter a valid profession.';
      } else if (errorText.includes('description') && (errorText.includes('too short') || errorText.includes('required'))) {
        userErrorMessage = 'Profile description must be at least 10 characters long.';
      } else if (errorText.includes('must be at least 16 years old')) {
        userErrorMessage = 'You must be at least 16 years old to create an account.';
      } else if (errorText.includes('date of birth') && (errorText.includes('invalid') || errorText.includes('required'))) {
        userErrorMessage = 'Please enter a valid date of birth in YYYY-MM-DD format.';
      } else if (errorText.includes('profile picture') && errorText.includes('invalid')) {
        userErrorMessage = 'There was an issue with your profile picture. Please try uploading again.';
      } else if (errorText.includes('user not found') || errorText.includes('not found')) {
        userErrorMessage = 'User account not found. Please try logging in again.';
      } else if (errorText.includes('unauthorized') || errorText.includes('authentication')) {
        userErrorMessage = 'Session expired. Please refresh the page and try again.';
      } else if (errorText.includes('validation') || errorText.includes('invalid')) {
        userErrorMessage = 'Please check your information and try again.';
      } else if (errorText.includes('network') || errorText.includes('connection')) {
        userErrorMessage = 'Network error. Please check your connection and try again.';
      }
    } else if (e.response?.data?.errors) {
      // Handle different error structure but don't expose raw errors
      userErrorMessage = "There was an issue with your submission. Please check your information and try again.";
    } else if (e.message && e.message.includes('NetworkError')) {
      userErrorMessage = "Network error. Please check your connection and try again.";
    } else if (e.message && e.message.includes('timeout')) {
      userErrorMessage = "Request timeout. Please try again.";
    }
    
    // Display user-friendly error messages
    setError(userErrorMessage);
    toast.error(userErrorMessage);
  } finally {
    setLoading(false);
  }
};
```

### 3. Add Pre-submission Validation

**File**: `CompleteProfile.jsx`

Add this helper function before the `handleSubmit` function:

```javascript
const validateAllFields = () => {
  const errors = [];
  
  // Required fields check
  if (!firstName?.trim()) errors.push('First name is required');
  if (!lastName?.trim()) errors.push('Last name is required');
  if (!email?.trim()) errors.push('Email is required');
  if (!phoneNumber?.trim()) errors.push('Phone number is required');
  if (!city?.trim()) errors.push('City is required');
  if (!profileDescription?.trim()) errors.push('Profile description is required');
  if (!dateOfBirth) errors.push('Date of birth is required');
  if (!role) errors.push('Role is required');
  if (!intention) errors.push('Intention is required');
  
  if (errors.length > 0) {
    setError(`Missing required fields: ${errors.join(', ')}`);
    return false;
  }
  
  return true;
};
```

Then call this function at the start of `handleSubmit`:

```javascript
const handleSubmit = async (e) => {
  e.preventDefault();
  if (!validateStep()) return;
  if (!validateAllFields()) return; // Add this line
  // ...rest of function
};
```

---

## Testing Checklist

After implementing these changes, test the following scenarios:

### ✅ Required Field Validation
- [ ] Try submitting without `dateOfBirth` - should show error
- [ ] Try submitting without `firstName` - should show error
- [ ] Try submitting without `profileDescription` - should show error

### ✅ Landlord vs Tenant Flow
- [ ] Complete profile as TENANT - should not include landlord fields
- [ ] Complete profile as LANDLORD - should include all landlord fields
- [ ] Switch between intentions and verify role auto-selection

### ✅ Error Handling
- [ ] Test with invalid email format
- [ ] Test with duplicate email (if you have test data)
- [ ] Test with network disconnection
- [ ] Test with expired session token

### ✅ Backend Communication
- [ ] Verify GraphQL mutation is sent with all required variables
- [ ] Check browser network tab for proper request structure
- [ ] Confirm successful profile completion updates user context

---

## Summary of Changes

1. **Enhanced Validation**: Made `dateOfBirth` validation mandatory in step 3
2. **Fixed Variable Handling**: Always send all GraphQL variables (null for unused ones)
3. **Improved Error Handling**: Added comprehensive backend error message mapping
4. **ID Conversion**: Explicit string conversion for user ID
5. **Pre-submission Check**: Added final validation before GraphQL request

These changes ensure 100% compatibility with your backend GraphQL schema and controller validation logic.
