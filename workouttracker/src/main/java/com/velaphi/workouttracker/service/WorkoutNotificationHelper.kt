package com.velaphi.workouttracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.velaphi.workouttracker.R
import com.velaphi.workouttracker.MainActivity

class WorkoutNotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "workout_notifications"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_WORKOUT = "com.velaphi.workouttracker.STOP_WORKOUT"
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for active workouts"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showWorkoutInProgressNotification(exerciseName: String, duration: Long) {
        // Check if notifications are enabled
        if (!areNotificationsEnabled()) {
            println("WorkoutNotificationHelper: Notifications not enabled, skipping notification")
            return
        }
        
        val stopIntent = Intent(context, WorkoutNotificationReceiver::class.java).apply {
            action = ACTION_STOP_WORKOUT
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Add extra to indicate we want to open the workout tracker
            putExtra("open_screen", "tracker")
        }
        
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Workout in Progress")
            .setContentText("$exerciseName - ${formatDuration(duration)}")
            .setSmallIcon(R.drawable.ic_workout)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_stop,
                "Stop Workout",
                stopPendingIntent
            )
            .setContentIntent(openAppPendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun dismissWorkoutNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationManager.areNotificationsEnabled()
        } else {
            true // For older versions, assume notifications are enabled
        }
    }
    
    private fun formatDuration(duration: Long): String {
        val minutes = duration / (1000 * 60)
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
