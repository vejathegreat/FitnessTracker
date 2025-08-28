package com.velaphi.mealplan

import com.velaphi.core.data.model.HealthyRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Test configuration utilities for the mealplan module
 */
object TestConfig {

    /**
     * Creates a test recipe for testing purposes
     */
    fun createTestRecipe(
        id: String = "test_id",
        title: String = "Test Recipe",
        description: String = "Test description",
        imageUrl: String = "https://example.com/test.jpg",
        calories: Double = 200.0,
        protein: Double = 15.0,
        carbs: Double = 25.0,
        fat: Double = 8.0,
        fiber: Double = 3.0,
        servings: Int = 1
    ): HealthyRecipe {
        return HealthyRecipe(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            servings = servings
        )
    }

    /**
     * Creates a list of test recipes
     */
    fun createTestRecipes(count: Int): List<HealthyRecipe> {
        return (1..count).map { index ->
            createTestRecipe(
                id = "test_id_$index",
                title = "Test Recipe $index",
                description = "Test description $index"
            )
        }
    }

    /**
     * Sets up test dispatcher for coroutines testing
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setupTestDispatcher(dispatcher: TestDispatcher = UnconfinedTestDispatcher()) {
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Resets test dispatcher
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun resetTestDispatcher() {
        Dispatchers.resetMain()
    }

    /**
     * Common test barcodes for testing
     */
    object TestBarcodes {
        const val VALID_BARCODE = "1234567890123"
        const val EMPTY_BARCODE = ""
        const val SPECIAL_CHARS_BARCODE = "ABC-123_456@#$%"
        const val LONG_BARCODE = "1".repeat(100)
        const val NETWORK_ERROR_BARCODE = "network_error_barcode"
        const val EMPTY_RESULTS_BARCODE = "empty_results_barcode"
        const val EXCEPTION_BARCODE = "exception_barcode"
    }

    /**
     * Common test error messages
     */
    object TestErrors {
        const val NETWORK_ERROR = "Network connection failed"
        const val DATABASE_ERROR = "Database connection failed"
        const val BARCODE_NOT_FOUND = "Barcode not found"
        const val UNEXPECTED_ERROR = "Unexpected error occurred"
        const val PERMISSION_DENIED = "Camera permission denied"
    }
}
