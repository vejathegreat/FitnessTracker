package com.velaphi.workouttracker

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.core.data.WorkoutExercise
import com.velaphi.core.data.WorkoutGoal
import com.velaphi.core.data.WorkoutGoalRepository
import com.velaphi.workouttracker.service.WorkoutService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkoutViewModel : ViewModel() {
    
    private val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()
    
    private val _workoutDuration = MutableStateFlow(0L)
    val workoutDuration: StateFlow<Long> = _workoutDuration.asStateFlow()
    
    private val _selectedExercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val selectedExercises: StateFlow<List<WorkoutExercise>> = _selectedExercises.asStateFlow()
    
    private val _workoutGoals = MutableStateFlow<List<WorkoutGoal>>(emptyList())
    val workoutGoals: StateFlow<List<WorkoutGoal>> = _workoutGoals.asStateFlow()
    
    private var timerJob: kotlinx.coroutines.Job? = null
    private var workoutStartTime: Long = 0
    private var goalRepository: WorkoutGoalRepository? = null
    private var goalsObserverJob: kotlinx.coroutines.Job? = null
    
    // Getter for goalRepository (for testing purposes)
    val goalRepositoryForTesting: WorkoutGoalRepository?
        get() = goalRepository
    
    fun initializeRepository(context: Context) {
        if (goalRepository == null) {
            goalRepository = WorkoutGoalRepository.getInstance(context)
            observeGoals()
        }
    }
    
    // Method to refresh goals from repository (called when goals are updated in Goal Manager)
    fun refreshGoals(context: Context) {
        if (goalRepository == null) {
            goalRepository = WorkoutGoalRepository.getInstance(context)
        }
        // Restart observation to ensure we get the latest goals
        goalsObserverJob?.cancel()
        observeGoals()
    }
    
    // Force refresh goals from the repository
    fun forceRefreshGoals() {
        goalRepository?.refreshGoals()
    }
    
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
            context.startForegroundService(intent)
            _workoutState.value = WorkoutState.ACTIVE
            workoutStartTime = System.currentTimeMillis()
            startDurationTimer()
            
            // Goals will be automatically updated via observeGoals()
        }
    }
    
    fun stopWorkout(context: Context) {
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_STOP_WORKOUT
        }
        context.startService(intent)
        _workoutState.value = WorkoutState.IDLE
        _workoutDuration.value = 0L
        workoutStartTime = 0
        timerJob?.cancel()
        timerJob = null
        
        // Clear active goal
        goalRepository?.clearActiveGoal()
        // Goals will be automatically updated via observeGoals()
    }
    

    
    fun selectExercise(exercise: WorkoutExercise) {
        val currentSelected = _selectedExercises.value.toMutableList()
        if (!currentSelected.any { it.id == exercise.id }) {
            currentSelected.add(exercise)
            _selectedExercises.value = currentSelected
        }
    }
    
    fun deselectExercise(exercise: WorkoutExercise) {
        val currentSelected = _selectedExercises.value.toMutableList()
        currentSelected.removeAll { it.id == exercise.id }
        _selectedExercises.value = currentSelected
    }
    
    fun clearSelectedExercises() {
        _selectedExercises.value = emptyList()
    }
    
    fun selectExerciseAsGoal(exercise: WorkoutExercise) {
        goalRepository?.selectGoal(exercise)
        // Goals will be automatically updated via observeGoals()
    }
    
    fun checkWorkoutStatus(context: Context) {
        val prefs = context.getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean(WorkoutService.PREF_WORKOUT_ACTIVE, false)
        val startTime = prefs.getLong(WorkoutService.PREF_WORKOUT_START_TIME, 0)
        
        if (isActive && startTime > 0) {
            _workoutState.value = WorkoutState.ACTIVE
            workoutStartTime = startTime
            val currentDuration = System.currentTimeMillis() - startTime
            _workoutDuration.value = currentDuration
            
            // Only start timer if it's not already running
            if (timerJob?.isActive != true) {
                startDurationTimer()
            }
        } else {
            _workoutState.value = WorkoutState.IDLE
            _workoutDuration.value = 0L
            workoutStartTime = 0
            timerJob?.cancel()
            timerJob = null
        }
    }
    
    fun syncWithService(context: Context) {
        if (_workoutState.value == WorkoutState.ACTIVE) {
            val prefs = context.getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
            val startTime = prefs.getLong(WorkoutService.PREF_WORKOUT_START_TIME, 0)
            if (startTime > 0) {
                workoutStartTime = startTime
                val currentDuration = System.currentTimeMillis() - startTime
                _workoutDuration.value = currentDuration
            }
        }
    }
    

    
    private fun observeGoals() {
        goalRepository?.let { repo ->
            // Cancel any existing observation
            goalsObserverJob?.cancel()
            
            // Observe the repository's goals StateFlow for real-time updates
            goalsObserverJob = viewModelScope.launch {
                repo.goals.collect { goals ->
                    _workoutGoals.value = goals
                    // Debug: Log the goals being loaded
                    println("WorkoutViewModel: Loaded ${goals.size} goals: ${goals.map { it.exercise.name }}")
                }
            }
        }
    }
    
    private fun startDurationTimer() {
        // Cancel existing timer if running
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            while (_workoutState.value == WorkoutState.ACTIVE) {
                if (workoutStartTime > 0) {
                    val currentDuration = System.currentTimeMillis() - workoutStartTime
                    _workoutDuration.value = currentDuration
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        goalsObserverJob?.cancel()
    }
}

enum class WorkoutState {
    IDLE,
    ACTIVE
}
