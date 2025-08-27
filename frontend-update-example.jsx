// Updated GraphQL mutation for your frontend CompleteProfile component
// Replace the existing updateUser mutation with this completeProfile mutation

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

// Updated variables object for the mutation
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
  // Landlord-specific fields
  ...(role === 'LANDLORD' && {
    openVisitations,
    autoAcceptBooking,
    visitDuration: visitDuration ? visitDuration.replace(/\s+/g, '_').toUpperCase() : null, // Convert to enum format like THIRTY_MINUTES
    bufferTimeHours: bufferTimeHours ? parseInt(bufferTimeHours) : null
  })
};

// Updated handleSubmit function
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
        dateOfBirth: dateOfBirth, // Backend expects this to be required
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

      // ✅ Enhanced success message mentioning email magic link
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

      // Handle GraphQL errors properly with sanitized user messages
      let userErrorMessage = "Failed to complete profile. Please try again.";

      if (e.response?.errors?.length > 0) {
        // Extract GraphQL error messages for analysis (not direct display)
        const graphqlErrors = e.response.errors.map(err => err.message).join('. ');
        const errorText = graphqlErrors.toLowerCase();

        // Handle specific error types with user-friendly messages
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
        } else if (errorText.includes('validation') || errorText.includes('invalid')) {
          userErrorMessage = 'Please check your information and try again.';
        } else if (errorText.includes('unauthorized') || errorText.includes('authentication')) {
          userErrorMessage = 'Session expired. Please refresh the page and try again.';
        } else if (errorText.includes('network') || errorText.includes('connection')) {
          userErrorMessage = 'Network error. Please check your connection and try again.';
        } else if (errorText.includes('not found')) {
          userErrorMessage = 'User account not found. Please try logging in again.';
        }
      } else if (e.response?.data?.errors) {
        // Handle different error structure but don't expose raw errors
        userErrorMessage = "There was an issue with your submission. Please check your information and try again.";
      } else if (e.message && e.message.includes('NetworkError')) {
        userErrorMessage = "Network error. Please check your connection and try again.";
      } else if (e.message && e.message.includes('timeout')) {
        userErrorMessage = "Request timeout. Please try again.";
      }

      // Always display user-friendly error messages
      setError(userErrorMessage);
      toast.error(userErrorMessage);
    } finally {
      setLoading(false);
    }
  };
