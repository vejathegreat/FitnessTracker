package com.velaphi.workouttracker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.velaphi.core.viewmodel.WorkoutViewModel
import com.velaphi.core.data.*
import com.velaphi.workouttracker.service.WorkoutService
import java.util.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutTrackerViewModel : WorkoutViewModel() {
    
    private var currentExercise: WorkoutExercise? = null
    private var sessionRepository: WorkoutSessionRepository? = null
    
    private val _lastWorkoutData = MutableStateFlow<LastWorkoutData?>(null)
    val lastWorkoutData: StateFlow<LastWorkoutData?> = _lastWorkoutData.asStateFlow()
    
    fun initializeSessionRepository(context: Context) {
        if (sessionRepository == null) {
            sessionRepository = WorkoutSessionRepository.getInstance(context)
            println("WorkoutTrackerViewModel: Initialized session repository")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun startWorkout(context: Context, exercise: WorkoutExercise? = null) {
        // If no specific exercise is provided, use the first selected goal
        val targetExercise = exercise ?: _workoutGoals.value.firstOrNull { it.isSelected }?.exercise
        
        if (targetExercise != null) {
            // Set this goal as active
            goalRepository?.setActiveGoal(targetExercise)
            
            // Store workout start info
            workoutStartTime = System.currentTimeMillis()
            currentExercise = targetExercise
            
            val intent = Intent(context, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_START_WORKOUT
                putExtra(WorkoutService.EXTRA_EXERCISE_ID, targetExercise.id)
            }
            context.startForegroundService(intent)
            _workoutState.value = com.velaphi.core.domain.WorkoutState.ACTIVE
            startDurationTimer()
            
            // Goals will be automatically updated via observeGoals()
        }
    }
    
    fun stopWorkout(context: Context) {
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_STOP_WORKOUT
        }
        context.startService(intent)
        _workoutState.value = com.velaphi.core.domain.WorkoutState.IDLE
        
        // Calculate workout duration
        val workoutDuration = if (workoutStartTime > 0) {
            System.currentTimeMillis() - workoutStartTime
        } else 0L
        
        // Store workout data for completion dialog
        _lastWorkoutData.value = LastWorkoutData(
            exerciseName = currentExercise?.name ?: "Unknown Exercise",
            duration = workoutDuration,
            caloriesBurned = estimateCalories(workoutDuration, currentExercise?.category)
        )
        
        // Save workout session to repository
        if (workoutDuration > 0 && currentExercise != null) {
            saveWorkoutSessionToRepository(context, workoutDuration)
        }
        
        _workoutDuration.value = 0L
        workoutStartTime = 0
        currentExercise = null
        timerJob?.cancel()
        timerJob = null
        
        // Clear active goal
        goalRepository?.clearActiveGoal()
        // Goals will be automatically updated via observeGoals()
    }
    
    fun saveWorkoutSession(session: WorkoutSession) {
        viewModelScope.launch {
            sessionRepository?.saveWorkoutSession(session)
            // Clear the last workout data after saving
            _lastWorkoutData.value = null
        }
    }
    
    fun clearLastWorkoutData() {
        _lastWorkoutData.value = null
    }
    
    private fun saveWorkoutSessionToRepository(context: Context, duration: Long) {
        if (sessionRepository == null) {
            sessionRepository = WorkoutSessionRepository.getInstance(context)
            println("WorkoutTrackerViewModel: Initialized session repository")
        }
        
        // Estimate calories based on duration and exercise type
        val caloriesBurned = estimateCalories(duration, currentExercise?.category)
        
        // Determine intensity based on duration (simplified logic)
        val intensity = when {
            duration < 15 * 60 * 1000 -> WorkoutIntensity.LIGHT // Less than 15 minutes
            duration < 45 * 60 * 1000 -> WorkoutIntensity.MODERATE // 15-45 minutes
            else -> WorkoutIntensity.INTENSE // More than 45 minutes
        }
        
        val session = WorkoutSession(
            id = UUID.randomUUID().toString(),
            exercise = currentExercise!!,
            startTime = Date(workoutStartTime),
            endTime = Date(System.currentTimeMillis()),
            duration = duration,
            intensity = intensity,
            caloriesBurned = caloriesBurned
        )
        
        println("WorkoutTrackerViewModel: Saving workout session - ${session.exercise.name}, duration: ${duration/1000}s, calories: $caloriesBurned")
        sessionRepository?.saveWorkoutSession(session)
        println("WorkoutTrackerViewModel: Workout session saved successfully")
    }
    
    private fun estimateCalories(duration: Long, category: ExerciseCategory?): Int {
        val minutes = duration / (1000 * 60)
        
        // Base calorie burn per minute for different exercise types
        val caloriesPerMinute = when (category) {
            ExerciseCategory.CARDIO -> 8
            ExerciseCategory.STRENGTH -> 6
            ExerciseCategory.FLEXIBILITY -> 3
            ExerciseCategory.BALANCE -> 4
            ExerciseCategory.SPORTS -> 7
            else -> 5 // Default
        }
        
        return (minutes * caloriesPerMinute).toInt()
    }
    
    fun checkWorkoutStatus(context: Context) {
        val prefs = context.getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean(WorkoutService.PREF_WORKOUT_ACTIVE, false)
        val startTime = prefs.getLong(WorkoutService.PREF_WORKOUT_START_TIME, 0)
        
        if (isActive && startTime > 0) {
            _workoutState.value = com.velaphi.core.domain.WorkoutState.ACTIVE
            workoutStartTime = startTime
            val currentDuration = System.currentTimeMillis() - startTime
            _workoutDuration.value = currentDuration
            
            // Only start timer if it's not already running
            if (timerJob?.isActive != true) {
                startDurationTimer()
            }
        } else {
            _workoutState.value = com.velaphi.core.domain.WorkoutState.IDLE
            _workoutDuration.value = 0L
            workoutStartTime = 0
            timerJob?.cancel()
            timerJob = null
        }
    }
    
    fun syncWithService(context: Context) {
        if (_workoutState.value == com.velaphi.core.domain.WorkoutState.ACTIVE) {
            val prefs = context.getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
            val startTime = prefs.getLong(WorkoutService.PREF_WORKOUT_START_TIME, 0)
            if (startTime > 0) {
                workoutStartTime = startTime
                val currentDuration = System.currentTimeMillis() - startTime
                _workoutDuration.value = currentDuration
            }
        }
    }
}

data class LastWorkoutData(
    val exerciseName: String,
    val duration: Long,
    val caloriesBurned: Int
)
