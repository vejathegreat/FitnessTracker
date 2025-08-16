package com.velaphi.core.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.core.data.WorkoutExercise
import com.velaphi.core.data.WorkoutGoal
import com.velaphi.core.data.WorkoutGoalRepository
import com.velaphi.core.domain.WorkoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

abstract class WorkoutViewModel : ViewModel() {
    
    protected val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()
    
    protected val _workoutDuration = MutableStateFlow(0L)
    val workoutDuration: StateFlow<Long> = _workoutDuration.asStateFlow()
    
    protected val _selectedExercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val selectedExercises: StateFlow<List<WorkoutExercise>> = _selectedExercises.asStateFlow()
    
    protected val _workoutGoals = MutableStateFlow<List<WorkoutGoal>>(emptyList())
    val workoutGoals: StateFlow<List<WorkoutGoal>> = _workoutGoals.asStateFlow()
    
    protected var timerJob: kotlinx.coroutines.Job? = null
    protected var workoutStartTime: Long = 0
    protected var goalRepository: WorkoutGoalRepository? = null
    protected var goalsObserverJob: kotlinx.coroutines.Job? = null
    
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
    
    protected fun observeGoals() {
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
    
    protected fun startDurationTimer() {
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
