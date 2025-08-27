# Frontend Fixes for CompleteProfile Component

## Issues Found and Fixed:

### 1. **ID Parameter Handling**
- **Issue**: Backend expects UUID but frontend might not handle ID conversion properly
- **Fix**: Ensure `user.id.toString()` is called to convert ID to string format

### 2. **Required Field Validation**
- **Issue**: Backend requires `dateOfBirth` as non-null, but frontend validation doesn't enforce this consistently
- **Fix**: Update validation to make `dateOfBirth` required in all cases

### 3. **Variable Preparation**
- **Issue**: Conditional inclusion of landlord variables could cause GraphQL errors
- **Fix**: Always include all variables but set them to null for non-landlord users

### 4. **Error Messages**
- **Issue**: Generic error handling doesn't cover backend-specific validation messages
- **Fix**: Enhanced error message mapping for backend validation errors

## Required Changes to Your Frontend:

### 1. Update the `validateStep` function for step 3:

```javascript
if (step === 3) {
  if (!profileDescription.trim()) {
    errors.push('Profile description is required.');
    toast.error('Please provide a short description about yourself.');
  } else if (profileDescription.trim().length < 10) {
    errors.push('Profile description must be at least 10 characters.');
    toast.error('Please provide a more detailed description (at least 10 characters).');
  }
  
  // REQUIRED: dateOfBirth is mandatory for profile completion
  if (!dateOfBirth) {
    errors.push('Date of birth is required.');
    toast.error('Please enter your date of birth.');
  } else {
    const dob = new Date(dateOfBirth);
    const today = new Date();
    const age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    
    const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate()) ? age - 1 : age;
    
    if (actualAge < 16) {
      errors.push('You must be at least 16 years old.');
      toast.error('You must be at least 16 years old.');
    } else if (actualAge > 120) {
      errors.push('Please enter a valid date of birth.');
      toast.error('Please enter a valid date of birth.');
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

### 2. Replace your entire `handleSubmit` function with the corrected version:

```javascript
const handleSubmit = async (e) => {
  e.preventDefault();
  if (!validateStep()) return;
  setError("");
  if (!role) {
    setError("Invalid intention selected; unable to determine role.");
    return;
  }
  
  // Ensure dateOfBirth is provided before submission
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
      id: user.id.toString(), // Ensure ID is passed as string
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

    // Add landlord-specific fields with proper null handling
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
    
    // Handle GraphQL errors with enhanced error messages
    let userErrorMessage = "Failed to complete profile. Please try again.";
    
    if (e.response?.errors?.length > 0) {
      const graphqlErrors = e.response.errors.map(err => err.message).join('. ');
      const errorText = graphqlErrors.toLowerCase();
      
      // Handle specific backend validation errors
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
      } else if (errorText.includes('description') && (errorText.includes('too short') || errorText.includes('required'))) {
        userErrorMessage = 'Profile description must be at least 10 characters long.';
      } else if (errorText.includes('must be at least 16 years old')) {
        userErrorMessage = 'You must be at least 16 years old to create an account.';
      } else if (errorText.includes('date of birth') && (errorText.includes('invalid') || errorText.includes('required'))) {
        userErrorMessage = 'Please enter a valid date of birth in YYYY-MM-DD format.';
      } else if (errorText.includes('user not found')) {
        userErrorMessage = 'User account not found. Please try logging in again.';
      } else if (errorText.includes('unauthorized') || errorText.includes('authentication')) {
        userErrorMessage = 'Session expired. Please refresh the page and try again.';
      } else if (errorText.includes('network') || errorText.includes('connection')) {
        userErrorMessage = 'Network error. Please check your connection and try again.';
      }
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

## Summary

Your frontend code will now:
1. ✅ Properly handle ID conversion to string
2. ✅ Enforce required fields according to backend validation
3. ✅ Always send all GraphQL variables (with proper null values)
4. ✅ Handle backend-specific error messages appropriately
5. ✅ Ensure `dateOfBirth` is always provided as required by backend

The main compatibility issues were around:
- Field requirements (especially `dateOfBirth`)
- Variable handling for conditional landlord fields  
- Error message mapping from backend validation

With these changes, your frontend should work perfectly with your backend GraphQL schema and controller.
