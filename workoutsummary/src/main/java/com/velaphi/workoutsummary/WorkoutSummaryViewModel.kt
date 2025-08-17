package com.velaphi.workoutsummary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.core.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class WorkoutSummaryViewModel : ViewModel() {
    
    private var sessionRepository: WorkoutSessionRepository? = null
    
    private val _workoutSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workoutSessions: StateFlow<List<WorkoutSession>> = _workoutSessions.asStateFlow()
    
    private val _workoutStats = MutableStateFlow(WorkoutStats())
    val workoutStats: StateFlow<WorkoutStats> = _workoutStats.asStateFlow()
    
    private val _completionStreak = MutableStateFlow(0)
    val completionStreak: StateFlow<Int> = _completionStreak.asStateFlow()
    
    private val _last5DaysData = MutableStateFlow<List<DayWorkoutData>>(emptyList())
    val last5DaysData: StateFlow<List<DayWorkoutData>> = _last5DaysData.asStateFlow()
    
    fun initializeRepository(context: Context) {
        if (sessionRepository == null) {
            sessionRepository = WorkoutSessionRepository.getInstance(context)
            loadWorkoutData()
        }
    }
    
    fun loadWorkoutData() {
        viewModelScope.launch {
            sessionRepository?.let { repo ->
                println("WorkoutSummaryViewModel: Loading workout data...")
                val sessions = repo.getAllWorkoutSessions()
                _workoutSessions.value = sessions
                println("WorkoutSummaryViewModel: Loaded ${sessions.size} workout sessions")
                
                val stats = repo.getAverageStatsForLastDays(5)
                _workoutStats.value = stats
                println("WorkoutSummaryViewModel: Stats - sessions: ${stats.totalSessions}, calories: ${stats.totalCalories}")
                
                val streak = repo.getCompletionStreak()
                _completionStreak.value = streak
                println("WorkoutSummaryViewModel: Completion streak: $streak days")
                
                val last5Days = generateLast5DaysData(repo)
                _last5DaysData.value = last5Days
                println("WorkoutSummaryViewModel: Generated data for ${last5Days.size} days")
            } ?: run {
                println("WorkoutSummaryViewModel: Session repository is null!")
            }
        }
    }
    
    private fun generateLast5DaysData(repo: WorkoutSessionRepository): List<DayWorkoutData> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<DayWorkoutData>()
        
        for (i in 4 downTo 0) {
            val date = calendar.time
            val daySessions = repo.getWorkoutSessionsForDate(date)
            
            val focus = if (daySessions.isNotEmpty()) {
                val categories = daySessions.map { it.exercise.category }
                when {
                    categories.any { it == ExerciseCategory.CARDIO } -> "Cardio"
                    categories.any { it == ExerciseCategory.STRENGTH } -> "Strength"
                    categories.any { it == ExerciseCategory.FLEXIBILITY } -> "Flexibility"
                    else -> "Mixed"
                }
            } else {
                "No workout"
            }
            
            val totalDuration = daySessions.sumOf { it.duration }
            val totalCalories = daySessions.sumOf { it.caloriesBurned }
            val intensity = if (daySessions.isNotEmpty()) {
                daySessions.maxOfOrNull { it.intensity.ordinal }?.let { WorkoutIntensity.values()[it] }
            } else null
            
            result.add(
                DayWorkoutData(
                    date = date,
                    focus = focus,
                    totalDuration = totalDuration,
                    totalCalories = totalCalories,
                    workoutCount = daySessions.size,
                    intensity = intensity,
                    sessions = daySessions
                )
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return result.reversed()
    }
    
    fun addWorkoutSession(session: WorkoutSession) {
        viewModelScope.launch {
            sessionRepository?.saveWorkoutSession(session)
            loadWorkoutData() // Refresh all data
        }
    }
    
    fun refreshData() {
        loadWorkoutData()
    }
}

data class DayWorkoutData(
    val date: Date,
    val focus: String,
    val totalDuration: Long,
    val totalCalories: Int,
    val workoutCount: Int,
    val intensity: WorkoutIntensity?,
    val sessions: List<WorkoutSession>
)
