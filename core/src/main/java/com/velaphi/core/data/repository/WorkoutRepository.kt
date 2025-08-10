package com.velaphi.core.data.repository

interface WorkoutRepository {
    suspend fun getWorkouts(): List<String>
    suspend fun addWorkout(workout: String)
}
