package com.velaphi.workouttracker

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.workouttracker.service.WorkoutService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkoutViewModel : ViewModel() {
    
    private val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()
    
    private val _workoutDuration = MutableStateFlow(0L)
    val workoutDuration: StateFlow<Long> = _workoutDuration.asStateFlow()
    
    fun startWorkout(context: Context) {
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_START_WORKOUT
        }
        context.startForegroundService(intent)
        _workoutState.value = WorkoutState.ACTIVE
        startDurationTimer()
    }
    
    fun stopWorkout(context: Context) {
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_STOP_WORKOUT
        }
        context.startService(intent)
        _workoutState.value = WorkoutState.IDLE
        _workoutDuration.value = 0L
    }
    
    private fun startDurationTimer() {
        viewModelScope.launch {
            while (_workoutState.value == WorkoutState.ACTIVE) {
                _workoutDuration.value += 1000
                kotlinx.coroutines.delay(1000)
            }
        }
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

enum class WorkoutState {
    IDLE,
    ACTIVE
}
