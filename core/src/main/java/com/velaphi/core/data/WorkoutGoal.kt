package com.velaphi.core.data

data class WorkoutGoal(
    val exercise: WorkoutExercise,
    val isSelected: Boolean = false,
    val priority: Int = 0, // 0 = no priority, higher numbers = higher priority
    val isActive: Boolean = false // whether this workout is currently running
)
