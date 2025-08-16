package com.velaphi.workouttracker

import android.content.Context
import android.content.Intent
import android.os.Build
import com.velaphi.core.viewmodel.WorkoutViewModel
import com.velaphi.core.data.WorkoutExercise
import com.velaphi.workouttracker.service.WorkoutService

class WorkoutTrackerViewModel : WorkoutViewModel() {
    
    fun startWorkout(context: Context, exercise: WorkoutExercise? = null) {
        // If no specific exercise is provided, use the first selected goal
        val targetExercise = exercise ?: _workoutGoals.value.firstOrNull { it.isSelected }?.exercise
        
        if (targetExercise != null) {
            // Set this goal as active
            goalRepository?.setActiveGoal(targetExercise)
            
            val intent = Intent(context, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_START_WORKOUT
                putExtra(WorkoutService.EXTRA_EXERCISE_ID, targetExercise.id)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _workoutState.value = com.velaphi.core.domain.WorkoutState.ACTIVE
            workoutStartTime = System.currentTimeMillis()
            startDurationTimer()

        }
    }
    
    fun stopWorkout(context: Context) {
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_STOP_WORKOUT
        }
        context.startService(intent)
        _workoutState.value = com.velaphi.core.domain.WorkoutState.IDLE
        _workoutDuration.value = 0L
        workoutStartTime = 0
        timerJob?.cancel()
        timerJob = null
        
        // Clear active goal
        goalRepository?.clearActiveGoal()
        // Goals will be automatically updated via observeGoals()
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
