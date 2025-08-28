# Fitness Tracker - Modular Android App

A comprehensive fitness tracking application built with modern Android development practices, featuring a modular architecture, MVVM pattern, and clean code principles.

## 🏗️ Architecture Overview

### Modular Architecture
The app follows a **feature-based modular architecture** where each feature is delivered as an independent module with proper access control and dependency management.

### Module Structure
```
FitnessTracker/
├── app/                    # Main application module
├── core/                   # Shared core module
├── workouttracker/         # Workout tracking feature
├── workoutsummary/         # Workout summary feature
├── goalmanager/           # Goal management feature
└── mealplan/              # Meal planning feature
```

## 🎯 Features

### 1. WorkoutTracker Module
- **Start, pause, and stop workouts** with session persistence
- **Workout resumption** even after app termination
- **Real-time duration tracking**
- **Calorie calculation** based on workout type
- **Multiple workout types** (Cardio, Strength, Flexibility, Sports, Other)
- **Access Control** Firebase Authentication

### 2. WorkoutSummary Module
- **Daily workout summaries** with detailed statistics
- **Date-based navigation** for historical data
- **Weekly statistics** aggregation
- **Visual progress indicators**

### 3. GoalManager Module
- **Set and track fitness goals** with progress monitoring
- **Multiple goal types** (Workout Frequency, Calories Burned, Duration, etc.)
- **Progress visualization** with completion tracking
- **Goal management** (add, update, delete)

### 4. MealPlan Module 
- **Nutrition tracking** with detailed macronutrient breakdown
- **Meal logging** with calorie and nutrition information
- **Recipe recommendations** based on fitness goals
- **Daily nutrition summaries**

## 🏛️ Design Patterns & Architecture

### MVVM (Model-View-ViewModel)
- **ViewModels** handle business logic and state management
- **UI State** is managed through StateFlow for reactive updates
- **Separation of concerns** between UI and business logic

### Repository Pattern
- **Repository interfaces** define data access contracts
- **Repository implementations** handle data operations
- **Abstraction layer** between data sources and business logic

### Dependency Injection (Hilt)
- **Singleton components** for shared dependencies
- **Module-based injection** for feature-specific dependencies
- **Clean dependency management** across modules

### Clean Architecture
- **Data Layer**: Room database, network APIs, repositories
- **Domain Layer**: Use cases, entities, repository interfaces
- **Presentation Layer**: ViewModels, UI components

## 🔧 Technical Implementation

### Core Technologies
- **Kotlin** with modern language features
- **Jetpack Compose** for modern UI development
- **Room Database** for local data persistence
- **Retrofit** for network operations
- **Hilt** for dependency injection
- **Coroutines & Flow** for asynchronous operations
- **WorkManager** for background tasks

### Database Design
```sql
-- Workouts table
CREATE TABLE workouts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    startTime TEXT NOT NULL,
    endTime TEXT,
    duration INTEGER DEFAULT 0,
    calories INTEGER DEFAULT 0,
    isActive INTEGER DEFAULT 0,
    notes TEXT
);

-- Goals table
CREATE TABLE goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL,
    targetValue REAL NOT NULL,
    currentValue REAL DEFAULT 0.0,
    unit TEXT NOT NULL,
    startDate TEXT NOT NULL,
    endDate TEXT NOT NULL,
    isActive INTEGER DEFAULT 1,
    isCompleted INTEGER DEFAULT 0
);

-- Meals table
CREATE TABLE meals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    calories INTEGER NOT NULL,
    protein REAL NOT NULL,
    carbs REAL NOT NULL,
    fat REAL NOT NULL,
    fiber REAL NOT NULL,
    dateTime TEXT NOT NULL,
    notes TEXT
);
```

### Network Layer
- **Nutrition API integration** for recipe recommendations
- **Retrofit with GSON** for JSON serialization
- **OkHttp logging** for debugging
- **Error handling** with proper fallbacks

## 🚀 Module Dependencies

### Access Control
- **Core module** exposes shared interfaces and models
- **Feature modules** depend only on core module
- **No cross-module dependencies** between features
- **Public APIs** clearly defined for each module

### Dependency Graph
```
app
├── core
├── workouttracker
│   └── core
├── workoutsummary
│   └── core
├── goalmanager
│   └── core
└── mealplan
    └── core
```

## 🧪 Testing Strategy

### Unit Testing
- **Repository layer** testing with MockK
- **ViewModel** testing with Turbine for Flow testing
- **Use case** testing for business logic
- **Test coverage** for critical paths

### Testing Example
```kotlin
@Test
fun `when workout is started, active workout should be returned`() = runTest {
    // Given
    val workout = Workout(name = "Test", type = CARDIO, startTime = LocalDateTime.now())
    coEvery { workoutRepository.insertWorkout(any()) } returns 1L
    
    // When
    viewModel.startWorkout("Test", CARDIO)
    
    // Then
    verify { workoutRepository.insertWorkout(any()) }
}
```

## 🔒 Security & Access Control

### Data Security
- **Local database** with SQLite encryption
- **Secure data storage** using DataStore for preferences
- **Input validation** on all user inputs
- **Error handling** without exposing sensitive information

### Module Access Control
- **Internal visibility** for module-specific implementations
- **Public APIs** only for necessary interfaces
- **Dependency inversion** through interfaces
- **Encapsulation** of module internals

## 📱 UI/UX Design

### Material Design 3
- **Modern Material You** theming
- **Dynamic color support**
- **Accessibility features**
- **Responsive design** for different screen sizes

### User Experience
- **Intuitive navigation** with bottom navigation
- **Real-time updates** with reactive UI
- **Loading states** and error handling
- **Smooth animations** and transitions

## 🔄 Background Processing

### Workout Persistence
- **WorkManager** for background workout tracking
- **Foreground service** for active workout sessions
- **Data persistence** across app restarts
- **Battery optimization** considerations

## 📈 Scalability Considerations

### Architecture Scaling
- **Feature modules** can be developed independently
- **Team scalability** with clear module boundaries
- **Feature flags** for gradual rollouts
- **A/B testing** support through modular design

### Performance Optimization
- **Lazy loading** of UI components
- **Database indexing** for efficient queries
- **Image caching** with Coil
- **Memory management** with proper lifecycle handling

## 🛠️ Development Setup

### Prerequisites
- Android Studio Hedgehog or later
- Kotlin 2.1.0
- Android SDK 35
- Minimum SDK 25

### Build Configuration
```kotlin
// Version catalog for dependency management
[versions]
agp = "8.9.3"
kotlin = "2.1.0"
composeBom = "2025.07.00"
hilt = "2.52"
```

### Running the App
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator

## 🎯 Future Enhancements

### Planned Features
- **Social features** for workout sharing
- **Advanced analytics** with charts and insights
- **Wearable integration** (Google Fit, Apple Health)
- **Cloud synchronization** for multi-device support
- **AI-powered recommendations** for workouts and nutrition

### Technical Improvements
- **Offline-first architecture** with sync capabilities
- **Real-time collaboration** features
- **Advanced caching** strategies
- **Performance monitoring** and analytics

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Follow the coding standards
4. Add tests for new features
5. Submit a pull request

## 📞 Images

<img width="1054" height="429" alt="Screenshot 2025-08-29 at 01 53 40" src="https://github.com/user-attachments/assets/b17be041-259a-48d4-9cf2-892b59a825fc" />


---

**Built with ❤️ using modern Android development practices** 
