package com.velaphi.workouttracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.core.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutTrackerViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<String>>(emptyList())
    val workouts: StateFlow<List<String>> = _workouts.asStateFlow()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            _workouts.value = workoutRepository.getWorkouts()
        }
    }

    fun addWorkout(workout: String) {
        viewModelScope.launch {
            workoutRepository.addWorkout(workout)
            loadWorkouts()
        }
    }
}
