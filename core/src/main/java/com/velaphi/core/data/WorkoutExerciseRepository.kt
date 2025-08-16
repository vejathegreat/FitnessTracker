package com.velaphi.core.data

object WorkoutExerciseRepository {
    
    fun getWorkoutExercises(): List<WorkoutExercise> {
        return listOf(
            // Strength Training
            WorkoutExercise(
                id = "push_ups",
                name = "Push-ups",
                category = ExerciseCategory.STRENGTH,
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
            ),
            WorkoutExercise(
                id = "pull_ups",
                name = "Pull-ups",
                category = ExerciseCategory.STRENGTH,
                muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.SHOULDERS)
            ),
            WorkoutExercise(
                id = "squats",
                name = "Squats",
                category = ExerciseCategory.STRENGTH,
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.ABS)
            ),
            WorkoutExercise(
                id = "deadlifts",
                name = "Deadlifts",
                category = ExerciseCategory.STRENGTH,
                muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
            ),
            WorkoutExercise(
                id = "planks",
                name = "Planks",
                category = ExerciseCategory.STRENGTH,
                muscleGroups = listOf(MuscleGroup.ABS)
            ),
            
            // Cardio
            WorkoutExercise(
                id = "running",
                name = "Running",
                category = ExerciseCategory.CARDIO,
                muscleGroups = listOf(MuscleGroup.FULL_BODY)
            ),
            WorkoutExercise(
                id = "cycling",
                name = "Cycling",
                category = ExerciseCategory.CARDIO,
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.CALVES)
            ),
            WorkoutExercise(
                id = "jumping_jacks",
                name = "Jumping Jacks",
                category = ExerciseCategory.CARDIO,
                muscleGroups = listOf(MuscleGroup.FULL_BODY)
            ),
            WorkoutExercise(
                id = "burpees",
                name = "Burpees",
                category = ExerciseCategory.CARDIO,
                muscleGroups = listOf(MuscleGroup.FULL_BODY)
            ),
            WorkoutExercise(
                id = "mountain_climbers",
                name = "Mountain Climbers",
                category = ExerciseCategory.CARDIO,
                muscleGroups = listOf(MuscleGroup.ABS, MuscleGroup.SHOULDERS)
            ),
            
            // Flexibility
            WorkoutExercise(
                id = "yoga_stretches",
                name = "Yoga Stretches",
                category = ExerciseCategory.FLEXIBILITY,
                muscleGroups = listOf(MuscleGroup.FULL_BODY)
            ),
            WorkoutExercise(
                id = "hamstring_stretches",
                name = "Hamstring Stretches",
                category = ExerciseCategory.FLEXIBILITY,
                muscleGroups = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES)
            ),
            WorkoutExercise(
                id = "hip_flexor_stretches",
                name = "Hip Flexor Stretches",
                category = ExerciseCategory.FLEXIBILITY,
                muscleGroups = listOf(MuscleGroup.GLUTES, MuscleGroup.QUADS)
            ),
            WorkoutExercise(
                id = "shoulder_stretches",
                name = "Shoulder Stretches",
                category = ExerciseCategory.FLEXIBILITY,
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)
            ),
            WorkoutExercise(
                id = "chest_stretches",
                name = "Chest Stretches",
                category = ExerciseCategory.FLEXIBILITY,
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)
            ),
            
            // Balance
            WorkoutExercise(
                id = "single_leg_stands",
                name = "Single Leg Stands",
                category = ExerciseCategory.BALANCE,
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.ABS)
            ),
            WorkoutExercise(
                id = "tree_pose",
                name = "Tree Pose",
                category = ExerciseCategory.BALANCE,
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.ABS)
            ),
            WorkoutExercise(
                id = "heel_to_toe_walk",
                name = "Heel to Toe Walk",
                category = ExerciseCategory.BALANCE,
                muscleGroups = listOf(MuscleGroup.CALVES, MuscleGroup.ABS)
            ),
            
            // Sports
            WorkoutExercise(
                id = "basketball_drills",
                name = "Basketball Drills",
                category = ExerciseCategory.SPORTS,
                muscleGroups = listOf(MuscleGroup.FULL_BODY)
            ),
            WorkoutExercise(
                id = "soccer_drills",
                name = "Soccer Drills",
                category = ExerciseCategory.SPORTS,
                muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.CALVES)
            ),
            WorkoutExercise(
                id = "tennis_drills",
                name = "Tennis Drills",
                category = ExerciseCategory.SPORTS,
                muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BICEPS, MuscleGroup.QUADS)
            )
        )
    }
    
    fun getExercisesByCategory(category: ExerciseCategory): List<WorkoutExercise> {
        return getWorkoutExercises().filter { it.category == category }
    }
    
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): List<WorkoutExercise> {
        return getWorkoutExercises().filter { it.muscleGroups.contains(muscleGroup) }
    }
}
