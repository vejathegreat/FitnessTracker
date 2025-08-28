package com.velaphi.workouttracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class WorkoutNotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        println("WorkoutNotificationReceiver: onReceive called with action: ${intent.action}")
        
        when (intent.action) {
            WorkoutNotificationHelper.ACTION_STOP_WORKOUT -> {
                println("WorkoutNotificationReceiver: Stopping workout...")
                
                // Send intent to the service to stop the workout
                val stopServiceIntent = Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_STOP_WORKOUT
                }
                context.startService(stopServiceIntent)
                
                println("WorkoutNotificationReceiver: Service stop intent sent")
                
                // Dismiss the notification
                val notificationHelper = WorkoutNotificationHelper(context)
                notificationHelper.dismissWorkoutNotification()
                
                println("WorkoutNotificationReceiver: Notification dismissed")
                
                // Send broadcast to update UI if app is open
                val updateIntent = Intent("com.velaphi.workouttracker.WORKOUT_STOPPED")
                context.sendBroadcast(updateIntent)
                
                println("WorkoutNotificationReceiver: UI update broadcast sent")
            }
            else -> {
                println("WorkoutNotificationReceiver: Unknown action: ${intent.action}")
            }
        }
    }
}
