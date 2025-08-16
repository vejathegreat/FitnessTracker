package com.velaphi.core.data

data class WorkoutExercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val muscleGroups: List<MuscleGroup>,
    val isSelected: Boolean = false
)

enum class ExerciseCategory {
    STRENGTH,
    CARDIO,
    FLEXIBILITY,
    BALANCE,
    SPORTS
}

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    ABS,
    GLUTES,
    QUADS,
    HAMSTRINGS,
    CALVES,
    FULL_BODY
}
