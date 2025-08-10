package com.velaphi.workouttracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WorkoutTrackerScreen(
    viewModel: WorkoutTrackerViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsState()
    var newWorkout by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Workout Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newWorkout,
                onValueChange = { newWorkout = it },
                label = { Text("Workout name") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newWorkout.isNotBlank()) {
                        viewModel.addWorkout(newWorkout)
                        newWorkout = ""
                    }
                }
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = workout,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}