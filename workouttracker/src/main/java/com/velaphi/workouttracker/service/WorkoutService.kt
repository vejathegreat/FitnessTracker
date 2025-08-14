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
import com.velaphi.workouttracker.MainActivity
import com.velaphi.workouttracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkoutService : Service() {
    
    companion object {
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_WORKOUT = "start_workout"
        const val ACTION_STOP_WORKOUT = "stop_workout"
        const val EXTRA_WORKOUT_DURATION = "workout_duration"
    }
    
    private var workoutJob: Job? = null
    private var workoutStartTime: Long = 0
    private var currentDuration: Long = 0
    
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_WORKOUT -> startWorkout()
            ACTION_STOP_WORKOUT -> stopWorkout()
        }
        return START_STICKY
    }
    
    private fun startWorkout() {
        workoutStartTime = System.currentTimeMillis()
        startForeground(NOTIFICATION_ID, createNotification(0))
        
        workoutJob = serviceScope.launch {
            while (isActive) {
                currentDuration = System.currentTimeMillis() - workoutStartTime
                updateNotification(currentDuration)
                delay(1000) // Update every second
            }
        }
    }
    
    private fun stopWorkout() {
        workoutJob?.cancel()
        stopForeground(true)
        stopSelf()
    }
    
    private fun createNotification(duration: Long): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, WorkoutService::class.java).apply {
            action = ACTION_STOP_WORKOUT
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workout in Progress")
            .setContentText(formatDuration(duration))
            .setSmallIcon(R.drawable.ic_workout)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(duration: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(duration))
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks active workout sessions"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        workoutJob?.cancel()
    }
}
