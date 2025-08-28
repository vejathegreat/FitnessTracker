package com.velaphi.workouttracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

import com.velaphi.core.data.WorkoutExerciseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkoutService : Service() {
    
    companion object {
        const val CHANNEL_ID = "workout_notifications"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_WORKOUT = "com.velaphi.workouttracker.START_WORKOUT"
        const val ACTION_STOP_WORKOUT = "com.velaphi.workouttracker.STOP_WORKOUT"
        const val EXTRA_WORKOUT_DURATION = "workout_duration"
        const val EXTRA_EXERCISE_ID = "exercise_id"
        
        // SharedPreferences keys
        const val PREF_WORKOUT_ACTIVE = "workout_active"
        const val PREF_WORKOUT_START_TIME = "workout_start_time"
        const val PREF_ACTIVE_EXERCISE_ID = "active_exercise_id"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var workoutJob: Job? = null
    private var workoutStartTime: Long = 0
    private var currentExerciseId: String? = null
    
    override fun onCreate() {
        super.onCreate()
        // Notification channel is created by WorkoutNotificationHelper
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("WorkoutService: onStartCommand called with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_WORKOUT -> {
                println("WorkoutService: Starting workout...")
                val exerciseId = intent.getStringExtra(EXTRA_EXERCISE_ID)
                startWorkout(exerciseId)
            }
            ACTION_STOP_WORKOUT -> {
                println("WorkoutService: Stopping workout...")
                stopWorkout()
            }
        }
        
        // If service is restarted by system, check if we need to resume workout
        if (intent?.action == null) {
            checkAndResumeWorkout()
        }
        
        return START_STICKY
    }
    
    private fun startWorkout(exerciseId: String?) {
        currentExerciseId = exerciseId
        workoutStartTime = System.currentTimeMillis()
        
        // Save workout state
        val prefs = getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(PREF_WORKOUT_ACTIVE, true)
            .putLong(PREF_WORKOUT_START_TIME, workoutStartTime)
            .putString(PREF_ACTIVE_EXERCISE_ID, exerciseId)
            .apply()
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Start workout timer
        startWorkoutTimer()
    }
    
    private fun stopWorkout() {
        println("WorkoutService: stopWorkout called")
        
        workoutJob?.cancel()
        workoutJob = null
        println("WorkoutService: Workout job cancelled")
        
        // Clear workout state
        val prefs = getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(PREF_WORKOUT_ACTIVE, false)
            .putLong(PREF_WORKOUT_START_TIME, 0)
            .putString(PREF_ACTIVE_EXERCISE_ID, null)
            .apply()
        println("WorkoutService: SharedPreferences cleared")
        
        // Stop foreground service
        stopForeground(true)
        stopSelf()
        println("WorkoutService: Service stopped")
    }
    
    private fun startWorkoutTimer() {
        workoutJob = serviceScope.launch {
            while (isActive) {
                delay(1000) // Update every second
                updateNotification()
            }
        }
    }
    
    private fun checkAndResumeWorkout() {
        val prefs = getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean(PREF_WORKOUT_ACTIVE, false)
        val startTime = prefs.getLong(PREF_WORKOUT_START_TIME, 0)
        val exerciseId = prefs.getString(PREF_ACTIVE_EXERCISE_ID, null)
        
        if (isActive && startTime > 0) {
            currentExerciseId = exerciseId
            workoutStartTime = startTime
            
            // Resume foreground service
            startForeground(NOTIFICATION_ID, createNotification())
            
            // Resume workout timer
            startWorkoutTimer()
        }
    }
    
    private fun createNotification(): Notification {
        val exerciseName = currentExerciseId?.let { id ->
            WorkoutExerciseRepository.getWorkoutExercises().find { it.id == id }?.name
        } ?: "Workout"
        
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("fitnesstracker://tracker")).apply {
            setPackage("com.velaphi.fitnesstracker")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$exerciseName in Progress")
            .setContentText("Tap to open app")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Don't stop the service when app is removed from recent tasks
    }
    
    fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
