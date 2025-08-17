package com.velaphi.core.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class WorkoutSession(
    @SerializedName("id")
    val id: String,
    @SerializedName("exercise")
    val exercise: WorkoutExercise,
    @SerializedName("startTime")
    val startTime: Date,
    @SerializedName("endTime")
    val endTime: Date,
    @SerializedName("duration")
    val duration: Long, // in milliseconds
    @SerializedName("intensity")
    val intensity: WorkoutIntensity,
    @SerializedName("caloriesBurned")
    val caloriesBurned: Int,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("moodBefore")
    val moodBefore: MoodRating? = null,
    @SerializedName("moodAfter")
    val moodAfter: MoodRating? = null,
    @SerializedName("energyBefore")
    val energyBefore: EnergyLevel? = null,
    @SerializedName("energyAfter")
    val energyAfter: EnergyLevel? = null
)

enum class WorkoutIntensity {
    @SerializedName("LIGHT")
    LIGHT,
    @SerializedName("MODERATE")
    MODERATE,
    @SerializedName("INTENSE")
    INTENSE
}

enum class MoodRating {
    @SerializedName("TERRIBLE")
    TERRIBLE,
    @SerializedName("BAD")
    BAD,
    @SerializedName("OKAY")
    OKAY,
    @SerializedName("GOOD")
    GOOD,
    @SerializedName("EXCELLENT")
    EXCELLENT
}

enum class EnergyLevel {
    @SerializedName("VERY_LOW")
    VERY_LOW,
    @SerializedName("LOW")
    LOW,
    @SerializedName("MEDIUM")
    MEDIUM,
    @SerializedName("HIGH")
    HIGH,
    @SerializedName("VERY_HIGH")
    VERY_HIGH
}
