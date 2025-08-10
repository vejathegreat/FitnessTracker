package com.velaphi.core.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor() : WorkoutRepository {
    private val workouts = mutableListOf<String>()

    override suspend fun getWorkouts(): List<String> = workouts.toList()

    override suspend fun addWorkout(workout: String) {
        workouts.add(workout)
    }
}
