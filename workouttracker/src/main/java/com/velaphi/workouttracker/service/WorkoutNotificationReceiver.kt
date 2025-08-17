package com.velaphi.workouttracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class WorkoutNotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WorkoutNotificationHelper.ACTION_STOP_WORKOUT -> {
                // Stop the workout by updating shared preferences
                val prefs = context.getSharedPreferences("workout_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean(WorkoutService.PREF_WORKOUT_ACTIVE, false)
                    .putLong(WorkoutService.PREF_WORKOUT_START_TIME, 0)
                    .apply()
                
                // Dismiss the notification
                val notificationHelper = WorkoutNotificationHelper(context)
                notificationHelper.dismissWorkoutNotification()
                
                // Send broadcast to update UI if app is open
                val updateIntent = Intent("com.velaphi.workouttracker.WORKOUT_STOPPED")
                context.sendBroadcast(updateIntent)
            }
        }
    }
}
