package com.velaphi.core.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class WorkoutSessionRepository private constructor(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("workout_sessions", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: WorkoutSessionRepository? = null
        
        fun getInstance(context: Context): WorkoutSessionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkoutSessionRepository(context.applicationContext).also { 
                    INSTANCE = it
                }
            }
        }
    }
    
    fun saveWorkoutSession(session: WorkoutSession) {
        println("WorkoutSessionRepository: Saving workout session - ${session.exercise.name}")
        val sessions = getAllWorkoutSessions().toMutableList()
        sessions.add(session)
        saveSessions(sessions)
        println("WorkoutSessionRepository: Session saved. Total sessions: ${sessions.size}")
    }
    
    fun getAllWorkoutSessions(): List<WorkoutSession> {
        val sessionsJson = prefs.getString("sessions", "[]")
        val type = object : TypeToken<ArrayList<WorkoutSession>>() {}.type
        val sessions = try {
            gson.fromJson<ArrayList<WorkoutSession>>(sessionsJson, type) ?: arrayListOf()
        } catch (e: Exception) {
            println("WorkoutSessionRepository: Error loading sessions: ${e.message}")
            arrayListOf()
        }
        println("WorkoutSessionRepository: Loaded ${sessions.size} sessions")
        return sessions
    }
    
    fun getWorkoutSessionsForLastDays(days: Int): List<WorkoutSession> {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
        
        return getAllWorkoutSessions().filter { it.endTime.after(cutoffDate) }
    }
    
    fun getWorkoutSessionsForDate(date: Date): List<WorkoutSession> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        return getAllWorkoutSessions().filter { session ->
            val sessionCalendar = Calendar.getInstance()
            sessionCalendar.time = session.endTime
            
            calendar.get(Calendar.YEAR) == sessionCalendar.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == sessionCalendar.get(Calendar.DAY_OF_YEAR)
        }
    }
    
    fun getCompletionStreak(): Int {
        val sessions = getAllWorkoutSessions().sortedByDescending { it.endTime }
        if (sessions.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        var currentDate = today
        var dayIndex = 0
        
        while (dayIndex < 30) { // Check up to 30 days
            val daySessions = getWorkoutSessionsForDate(currentDate)
            if (daySessions.isNotEmpty()) {
                streak++
            } else {
                break
            }
            
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            currentDate = calendar.time
            dayIndex++
        }
        
        return streak
    }
    
    fun getAverageStatsForLastDays(days: Int): WorkoutStats {
        val sessions = getWorkoutSessionsForLastDays(days)
        if (sessions.isEmpty()) return WorkoutStats()
        
        val totalDuration = sessions.sumOf { it.duration }
        val totalCalories = sessions.sumOf { it.caloriesBurned }
        val avgDuration = totalDuration / sessions.size
        val avgCalories = totalCalories / sessions.size
        
        return WorkoutStats(
            averageDuration = avgDuration,
            averageCalories = avgCalories,
            totalSessions = sessions.size,
            totalDuration = totalDuration,
            totalCalories = totalCalories
        )
    }
    
    private fun saveSessions(sessions: List<WorkoutSession>) {
        val sessionsJson = gson.toJson(sessions, object : TypeToken<ArrayList<WorkoutSession>>() {}.type)
        prefs.edit().putString("sessions", sessionsJson).apply()
    }
}

data class WorkoutStats(
    val averageDuration: Long = 0L,
    val averageCalories: Int = 0,
    val totalSessions: Int = 0,
    val totalDuration: Long = 0L,
    val totalCalories: Int = 0
)
