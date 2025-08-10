# Dagger Hilt and Room Setup

This document explains how Dagger Hilt and Room have been set up to make Room objects accessible throughout all modules.

## Overview

The project now uses Dagger Hilt for dependency injection, making Room database, DAOs, and repositories accessible from any module in the application.

## Setup Details

### 1. Build Configuration

All modules now have Hilt plugins and dependencies enabled:

```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
}
```

### 2. Application Class

The `FitnessTrackerApplication` class is annotated with `@HiltAndroidApp`:

```kotlin
@HiltAndroidApp
class FitnessTrackerApplication : Application()
```

### 3. Hilt Modules

#### Database Module (`DatabaseModule.kt`)
Provides Room database and DAOs:
- `FitnessDatabase`
- `WorkoutDao`
- `GoalDao`
- `MealDao`

#### Repository Module (`RepositoryModule.kt`)
Binds repository implementations to interfaces:
- `WorkoutRepository`
- `GoalRepository`
- `MealRepository`

### 4. Room Entities

All entities are properly annotated and uncommented:
- `Workout` - Workout tracking data
- `Goal` - Fitness goals
- `Meal` - Meal tracking data

### 5. DAOs

All DAOs are properly annotated and uncommented:
- `WorkoutDao` - CRUD operations for workouts
- `GoalDao` - CRUD operations for goals
- `MealDao` - CRUD operations for meals

### 6. Repositories

Repository implementations are properly injected with `@Inject`:

```kotlin
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository
```

## Usage Examples

### 1. In ViewModels (Recommended)

```kotlin
@HiltViewModel
class WorkoutTrackerViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    // Use repository methods
}
```

### 2. In Composables

```kotlin
@Composable
fun MyScreen() {
    val viewModel: WorkoutTrackerViewModel = hiltViewModel()
    // Use viewModel methods
}
```

### 3. Direct Repository Access (if needed)

```kotlin
@Composable
fun MyScreen() {
    val context = LocalContext.current
    val workoutRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RepositoryEntryPoint::class.java
        ).workoutRepository()
    }
    // Use repository directly
}
```

## Available Repositories

### WorkoutRepository
- `getAllWorkouts()`: Get all workouts
- `getWorkoutsByDate(date)`: Get workouts for specific date
- `getActiveWorkout()`: Get currently active workout
- `insertWorkout(workout)`: Insert new workout
- `updateWorkout(workout)`: Update existing workout
- `deleteWorkout(workout)`: Delete workout

### GoalRepository
- `getAllGoals()`: Get all goals
- `getActiveGoals()`: Get active (incomplete) goals
- `getGoalsByType(type)`: Get goals by type
- `insertGoal(goal)`: Insert new goal
- `updateGoal(goal)`: Update existing goal
- `deleteGoal(goal)`: Delete goal

### MealRepository
- `getAllMeals()`: Get all meals
- `getMealsByDate(date)`: Get meals for specific date
- `getMealsByType(type)`: Get meals by type
- `insertMeal(meal)`: Insert new meal
- `updateMeal(meal)`: Update existing meal
- `deleteMeal(meal)`: Delete meal

## Database Schema

### Workouts Table
- `id` (Primary Key)
- `type` (WorkoutType enum)
- `startTime` (LocalDateTime)
- `endTime` (LocalDateTime, nullable)
- `duration` (Long, seconds)
- `calories` (Int)
- `isActive` (Boolean)
- `notes` (String, nullable)

### Goals Table
- `id` (Primary Key)
- `type` (GoalType enum)
- `title` (String)
- `description` (String, nullable)
- `targetValue` (Double)
- `currentValue` (Double)
- `unit` (String)
- `deadline` (LocalDateTime, nullable)
- `isCompleted` (Boolean)
- `createdAt` (LocalDateTime)

### Meals Table
- `id` (Primary Key)
- `type` (MealType enum)
- `name` (String)
- `calories` (Int)
- `protein` (Double)
- `carbs` (Double)
- `fat` (Double)
- `date` (LocalDate)
- `notes` (String, nullable)

## Benefits

1. **Single Source of Truth**: All modules access the same database instance
2. **Type Safety**: Compile-time dependency injection
3. **Testability**: Easy to mock repositories for testing
4. **Modularity**: Each module can access data without tight coupling
5. **Scalability**: Easy to add new repositories and entities

## Testing

All repositories can be easily mocked for testing:

```kotlin
@HiltAndroidTest
class WorkoutTrackerViewModelTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var workoutRepository: WorkoutRepository
    
    // Test implementation
}
```

## Migration Notes

- All previously commented code has been uncommented
- Hilt annotations have been properly applied
- Room entities and DAOs are now active
- Repository implementations are properly injected
- ViewModels in all modules now use Hilt injection 