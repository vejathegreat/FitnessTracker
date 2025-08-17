package com.velaphi.core.data

import com.google.gson.annotations.SerializedName

data class WorkoutExercise(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: ExerciseCategory,
    @SerializedName("muscleGroups")
    val muscleGroups: List<MuscleGroup>,
    @SerializedName("isSelected")
    val isSelected: Boolean = false
)

enum class ExerciseCategory {
    @SerializedName("STRENGTH")
    STRENGTH,
    @SerializedName("CARDIO")
    CARDIO,
    @SerializedName("FLEXIBILITY")
    FLEXIBILITY,
    @SerializedName("BALANCE")
    BALANCE,
    @SerializedName("SPORTS")
    SPORTS
}

enum class MuscleGroup {
    @SerializedName("CHEST")
    CHEST,
    @SerializedName("BACK")
    BACK,
    @SerializedName("SHOULDERS")
    SHOULDERS,
    @SerializedName("BICEPS")
    BICEPS,
    @SerializedName("TRICEPS")
    TRICEPS,
    @SerializedName("FOREARMS")
    FOREARMS,
    @SerializedName("ABS")
    ABS,
    @SerializedName("GLUTES")
    GLUTES,
    @SerializedName("QUADS")
    QUADS,
    @SerializedName("HAMSTRINGS")
    HAMSTRINGS,
    @SerializedName("CALVES")
    CALVES,
    @SerializedName("FULL_BODY")
    FULL_BODY
}
