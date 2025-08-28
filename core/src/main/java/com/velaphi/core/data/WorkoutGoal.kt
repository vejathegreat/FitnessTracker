package com.velaphi.core.data

data class WorkoutGoal(
    val exercise: WorkoutExercise,
    val isSelected: Boolean = false,
    val priority: Int = 0,
    val isActive: Boolean = false
)
