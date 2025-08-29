# FitQuest Authentication Module

This module provides a complete authentication system for the FitQuest app using Firebase Authentication, MVVM architecture, and Jetpack Compose.

## Features

- **Email/Password Authentication**: Sign up, sign in, and sign out functionality
- **User Profile Management**: View and update user profile information
- **Firebase Integration**: Uses Firebase Auth and Firestore for backend services
- **MVVM Architecture**: Clean separation of concerns with ViewModels and Repositories
- **Coroutines**: Asynchronous operations using Kotlin Coroutines
- **Modern UI**: Beautiful Material 3 design with Jetpack Compose

## Setup Instructions

### 1. Firebase Configuration

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Authentication with Email/Password sign-in method
3. Enable Firestore Database
4. Download `google-services.json` and place it in the `authetication/` directory
5. Add your Firebase project's SHA-1 fingerprint for Android apps

### 2. Dependencies

The module already includes all necessary dependencies:
- Firebase Auth and Firestore
- Hilt for dependency injection
- Jetpack Compose with Material 3
- Navigation Compose
- Coroutines and Flow

### 3. Usage

The authentication module provides the following screens:
- **LoginScreen**: Email and password sign-in
- **SignUpScreen**: New user registration
- **ProfileScreen**: User profile and account management
- **HomeScreen**: Main screen after successful authentication

### 4. Integration

To use this module in your main app:

1. Add the authentication module as a dependency
2. Initialize Firebase in your main app
3. Use the `AuthNavigation` composable for authentication flow
4. Observe authentication state changes

## Architecture

- **Data Layer**: Models, Repository interface and implementation
- **Domain Layer**: Use cases and business logic
- **Presentation Layer**: ViewModels, UI screens, and navigation
- **DI**: Hilt modules for dependency injection

## Security Features

- Password validation and confirmation
- Email verification status tracking
- Secure Firebase authentication
- User data stored in Firestore with proper security rules

## Customization

You can customize:
- UI themes and colors
- Validation rules
- Error messages
- Additional user profile fields
- Authentication methods (Google Sign-In, etc.)

## Testing

The module includes test files for:
- Unit tests
- Instrumented tests
- UI tests with Compose

## Support

For issues or questions, please refer to the main FitQuest project documentation.
