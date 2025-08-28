package com.velaphi.workouttracker.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.velaphi.core.data.*

@Composable
fun WorkoutCompletionDialog(
    exerciseName: String,
    duration: Long,
    caloriesBurned: Int,
    onDismiss: () -> Unit,
    onComplete: (WorkoutSession) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var moodBefore by remember { mutableStateOf<MoodRating?>(null) }
    var moodAfter by remember { mutableStateOf<MoodRating?>(null) }
    var energyBefore by remember { mutableStateOf<EnergyLevel?>(null) }
    var energyAfter by remember { mutableStateOf<EnergyLevel?>(null) }
    var intensity by remember { mutableStateOf(WorkoutIntensity.MODERATE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Workout Complete! 🎉",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Workout Summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoItem(
                                icon = Icons.Default.Timer,
                                text = formatDuration(duration)
                            )
                            InfoItem(
                                icon = Icons.Default.Whatshot,
                                text = "$caloriesBurned cal"
                            )
                        }
                    }
                }
                
                // Intensity Selection
                Column {
                    Text(
                        text = "Workout Intensity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WorkoutIntensity.values().forEach { workoutIntensity ->
                            FilterChip(
                                onClick = { intensity = workoutIntensity },
                                label = { Text(workoutIntensity.name.lowercase().capitalize()) },
                                selected = intensity == workoutIntensity,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
                
                // Mood Tracking
                Column {
                    Text(
                        text = "How did you feel?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Before",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            MoodRatingSelector(
                                selectedRating = moodBefore,
                                onRatingSelected = { moodBefore = it }
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "After",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            MoodRatingSelector(
                                selectedRating = moodAfter,
                                onRatingSelected = { moodAfter = it }
                            )
                        }
                    }
                }
                
                // Energy Tracking
                Column {
                    Text(
                        text = "Energy Levels",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Before",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            EnergyLevelSelector(
                                selectedLevel = energyBefore,
                                onLevelSelected = { energyBefore = it }
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "After",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            EnergyLevelSelector(
                                selectedLevel = energyAfter,
                                onLevelSelected = { energyAfter = it }
                            )
                        }
                    }
                }
                
                // Notes
                Column {
                    Text(
                        text = "Workout Notes (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("How was your workout? Any thoughts?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Create workout session with all the collected data
                    val session = WorkoutSession(
                        id = java.util.UUID.randomUUID().toString(),
                        exercise = WorkoutExercise(
                            id = "temp",
                            name = exerciseName,
                            category = ExerciseCategory.STRENGTH, // Default, could be enhanced
                            muscleGroups = emptyList()
                        ),
                        startTime = java.util.Date(System.currentTimeMillis() - duration),
                        endTime = java.util.Date(),
                        duration = duration,
                        intensity = intensity,
                        caloriesBurned = caloriesBurned,
                        notes = notes.takeIf { it.isNotBlank() },
                        moodBefore = moodBefore,
                        moodAfter = moodAfter,
                        energyBefore = energyBefore,
                        energyAfter = energyAfter
                    )
                    onComplete(session)
                }
            ) {
                Text("Save Workout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MoodRatingSelector(
    selectedRating: MoodRating?,
    onRatingSelected: (MoodRating) -> Unit
) {
    val ratings = listOf(
        MoodRating.TERRIBLE to "😞",
        MoodRating.BAD to "😕",
        MoodRating.OKAY to "😐",
        MoodRating.GOOD to "🙂",
        MoodRating.EXCELLENT to "😄"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ratings.forEach { (rating, emoji) ->
            FilterChip(
                onClick = { onRatingSelected(rating) },
                label = { Text(emoji) },
                selected = selectedRating == rating,
                modifier = Modifier.size(40.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun EnergyLevelSelector(
    selectedLevel: EnergyLevel?,
    onLevelSelected: (EnergyLevel) -> Unit
) {
    val levels = listOf(
        EnergyLevel.VERY_LOW to "🔋",
        EnergyLevel.LOW to "🔋",
        EnergyLevel.MEDIUM to "🔋",
        EnergyLevel.HIGH to "🔋",
        EnergyLevel.VERY_HIGH to "🔋"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        levels.forEachIndexed { index, (level, emoji) ->
            val batteryLevel = when (index) {
                0 -> "🔋" // Empty battery
                1 -> "🟡" // Low battery
                2 -> "🟠" // Medium battery
                3 -> "🟢" // High battery
                else -> "🟢" // Full battery
            }
            
            FilterChip(
                onClick = { onLevelSelected(level) },
                label = { Text(batteryLevel) },
                selected = selectedLevel == level,
                modifier = Modifier.size(40.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(duration: Long): String {
    val minutes = duration / (1000 * 60)
    val hours = minutes / 60
    return if (hours > 0) {
        "${hours}h ${minutes % 60}m"
    } else {
        "${minutes}m"
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}


