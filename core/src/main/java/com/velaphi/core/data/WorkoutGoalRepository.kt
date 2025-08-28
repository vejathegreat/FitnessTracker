package com.velaphi.core.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class WorkoutGoalRepository private constructor(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("workout_goals", Context.MODE_PRIVATE)
    private val _goals = MutableStateFlow<List<WorkoutGoal>>(emptyList())
    val goals: StateFlow<List<WorkoutGoal>> = _goals.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: WorkoutGoalRepository? = null
        
        fun getInstance(context: Context): WorkoutGoalRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkoutGoalRepository(context.applicationContext).also { 
                                    INSTANCE = it
                Timber.d("Created new singleton instance")
                }
            }.also {
                            if (INSTANCE != null) {
                Timber.d("Returning existing singleton instance")
            }
            }
        }
    }
    
    init {
        loadGoals()
    }
    
    fun selectGoal(exercise: WorkoutExercise) {
        val currentGoals = _goals.value.toMutableList()
        
        // Check if exercise is already a goal
        val existingGoalIndex = currentGoals.indexOfFirst { it.exercise.id == exercise.id }
        
        if (existingGoalIndex != -1) {
            // Update existing goal
            val existingGoal = currentGoals[existingGoalIndex]
            currentGoals[existingGoalIndex] = existingGoal.copy(
                isSelected = true,
                priority = getNextPriority()
            )
        } else {
            // Add new goal
            currentGoals.add(WorkoutGoal(
                exercise = exercise,
                isSelected = true,
                priority = getNextPriority()
            ))
        }
        
        // Sort by priority (highest first)
        currentGoals.sortByDescending { it.priority }
        _goals.value = currentGoals
        saveGoals()
        
        // Debug: Log the goal selection
        Timber.d("Added goal for ${exercise.name}, total goals: ${currentGoals.size}")
    }
    
    fun deselectGoal(exercise: WorkoutExercise) {
        val currentGoals = _goals.value.toMutableList()
        val goalIndex = currentGoals.indexOfFirst { it.exercise.id == exercise.id }
        
        if (goalIndex != -1) {
            currentGoals.removeAt(goalIndex)
            // Reorder priorities
            reorderPriorities(currentGoals)
            _goals.value = currentGoals
            saveGoals()
            
            // Debug: Log the goal deselection
            Timber.d("Removed goal for ${exercise.name}, total goals: ${currentGoals.size}")
        }
    }
    
    fun setActiveGoal(exercise: WorkoutExercise) {
        val currentGoals = _goals.value.toMutableList()
        
        // Set all goals as inactive first
        currentGoals.forEachIndexed { index, goal ->
            currentGoals[index] = goal.copy(isActive = false)
        }
        
        // Set the selected goal as active
        val goalIndex = currentGoals.indexOfFirst { it.exercise.id == exercise.id }
        if (goalIndex != -1) {
            currentGoals[goalIndex] = currentGoals[goalIndex].copy(isActive = true)
        }
        
        _goals.value = currentGoals
        saveGoals()
    }
    
    fun clearActiveGoal() {
        val currentGoals = _goals.value.toMutableList()
        currentGoals.forEachIndexed { index, goal ->
            currentGoals[index] = goal.copy(isActive = false)
        }
        _goals.value = currentGoals
        saveGoals()
    }
    
    fun getRandomExercises(count: Int): List<WorkoutExercise> {
        val allExercises = WorkoutExerciseRepository.getWorkoutExercises()
        val selectedGoals = _goals.value.filter { it.isSelected }.map { it.exercise }
        
        // Filter out already selected goals
        val availableExercises = allExercises.filter { exercise ->
            !selectedGoals.any { it.id == exercise.id }
        }
        
        // Return random exercises, or all available if count is greater
        return if (availableExercises.size <= count) {
            availableExercises
        } else {
            availableExercises.shuffled().take(count)
        }
    }
    
    fun hasSelectedGoals(): Boolean {
        return _goals.value.any { it.isSelected }
    }
    
    fun getActiveGoal(): WorkoutGoal? {
        return _goals.value.find { it.isActive }
    }
    
    // Method to manually refresh goals from storage
    fun refreshGoals() {
        loadGoals()
    }
    
    // Method to manually trigger a StateFlow update (for testing)
    fun triggerUpdate() {
        val currentGoals = _goals.value
        _goals.value = currentGoals
        Timber.d("Manually triggered StateFlow update with ${currentGoals.size} goals")
    }
    
    private fun getNextPriority(): Int {
        val currentGoals = _goals.value
        return if (currentGoals.isEmpty()) 1 else currentGoals.maxOf { it.priority } + 1
    }
    
    private fun reorderPriorities(goals: MutableList<WorkoutGoal>) {
        goals.forEachIndexed { index, goal ->
            goals[index] = goal.copy(priority = goals.size - index)
        }
    }
    
    private fun saveGoals() {
        val goalsJson = _goals.value.map { goal ->
            "${goal.exercise.id}|${goal.isSelected}|${goal.priority}|${goal.isActive}"
        }.joinToString(";")
        prefs.edit().putString("goals", goalsJson).apply()
        Timber.d("Saved goals to JSON: '$goalsJson'")
    }
    
    private fun loadGoals() {
        val goalsJson = prefs.getString("goals", "") ?: ""
        Timber.d("Loading goals from JSON: '$goalsJson'")
        
        if (goalsJson.isNotEmpty()) {
            val goalsList = goalsJson.split(";").mapNotNull { goalString ->
                val parts = goalString.split("|")
                if (parts.size == 4) {
                    val exercise = WorkoutExerciseRepository.getWorkoutExercises()
                        .find { it.id == parts[0] }
                    if (exercise != null) {
                        WorkoutGoal(
                            exercise = exercise,
                            isSelected = parts[1].toBoolean(),
                            priority = parts[2].toInt(),
                            isActive = parts[3].toBoolean()
                        )
                    } else null
                } else null
            }
            _goals.value = goalsList.sortedByDescending { it.priority }
            Timber.d("Loaded ${goalsList.size} goals: ${goalsList.map { it.exercise.name }}")
        } else {
            Timber.d("No goals found in storage")
        }
    }
}
