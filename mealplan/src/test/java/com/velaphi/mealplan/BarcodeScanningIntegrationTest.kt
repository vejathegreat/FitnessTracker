package com.velaphi.mealplan

import com.velaphi.core.data.FoodRepository
import com.velaphi.core.data.model.HealthyRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class BarcodeScanningIntegrationTest {

    @Mock
    private lateinit var mockFoodRepository: FoodRepository

    private lateinit var viewModel: MealPlanViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MealPlanViewModel(mockFoodRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `barcode scanning workflow should search recipes and update UI state`() = runTest {
        // Given
        val barcode = "1234567890123"
        val expectedRecipes = listOf(
            createTestRecipe("1", "Barcode Product 1"),
            createTestRecipe("2", "Barcode Product 2")
        )
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode)).thenReturn(Result.success(expectedRecipes))

        // When
        viewModel.searchRecipesByBarcode(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedRecipes, viewModel.recipes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify(mockFoodRepository).searchRecipesByBarcode(barcode)
    }

    @Test
    fun `barcode scanning should handle network errors gracefully`() = runTest {
        // Given
        val barcode = "network_error_barcode"
        val networkError = Exception("Network connection failed")
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode)).thenReturn(Result.failure(networkError))

        // When
        viewModel.searchRecipesByBarcode(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.recipes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("Barcode search failed: ${networkError.message}", viewModel.error.value)
    }

    @Test
    fun `barcode scanning should handle empty results`() = runTest {
        // Given
        val barcode = "empty_results_barcode"
        val emptyResults = emptyList<HealthyRecipe>()
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode)).thenReturn(Result.success(emptyResults))

        // When
        viewModel.searchRecipesByBarcode(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.recipes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `barcode scanning should handle malformed barcodes`() = runTest {
        // Given
        val malformedBarcode = "ABC-123_456@#$%"
        val expectedRecipes = listOf(createTestRecipe("1", "Malformed Barcode Product"))
        `when`(mockFoodRepository.searchRecipesByBarcode(malformedBarcode)).thenReturn(Result.success(expectedRecipes))

        // When
        viewModel.searchRecipesByBarcode(malformedBarcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedRecipes, viewModel.recipes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `barcode scanning should handle very long barcodes`() = runTest {
        // Given
        val longBarcode = "1".repeat(100) // 100 character barcode
        val expectedRecipes = listOf(createTestRecipe("1", "Long Barcode Product"))
        `when`(mockFoodRepository.searchRecipesByBarcode(longBarcode)).thenReturn(Result.success(expectedRecipes))

        // When
        viewModel.searchRecipesByBarcode(longBarcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedRecipes, viewModel.recipes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `barcode scanning should handle special characters in barcode`() = runTest {
        // Given
        val specialBarcode = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val expectedRecipes = listOf(createTestRecipe("1", "Special Characters Product"))
        `when`(mockFoodRepository.searchRecipesByBarcode(specialBarcode)).thenReturn(Result.success(expectedRecipes))

        // When
        viewModel.searchRecipesByBarcode(specialBarcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedRecipes, viewModel.recipes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `barcode scanning should handle repository exceptions`() = runTest {
        // Given
        val barcode = "exception_barcode"
        val repositoryException = RuntimeException("Repository internal error")
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode)).thenThrow(repositoryException)

        // When
        viewModel.searchRecipesByBarcode(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.recipes.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("Unexpected error in barcode search: ${repositoryException.message}", viewModel.error.value)
    }

    @Test
    fun `barcode scanning should maintain loading state during operation`() = runTest {
        // Given
        val barcode = "loading_test_barcode"
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode)).thenReturn(Result.success(emptyList()))

        // When
        viewModel.searchRecipesByBarcode(barcode)

        // Then
        assertTrue(viewModel.isLoading.value)

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `barcode scanning should clear previous errors on success`() = runTest {
        // Given
        val barcode = "success_after_error_barcode"
        val expectedRecipes = listOf(createTestRecipe("1", "Success After Error Product"))
        
        // First, set an error state
        viewModel.error.value = "Previous error"
        assertEquals("Previous error", viewModel.error.value)
        
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode)).thenReturn(Result.success(expectedRecipes))

        // When
        viewModel.searchRecipesByBarcode(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedRecipes, viewModel.recipes.value)
        assertNull(viewModel.error.value) // Error should be cleared
    }

    @Test
    fun `barcode scanning should handle concurrent requests`() = runTest {
        // Given
        val barcode1 = "concurrent_barcode_1"
        val barcode2 = "concurrent_barcode_2"
        val recipes1 = listOf(createTestRecipe("1", "Concurrent Product 1"))
        val recipes2 = listOf(createTestRecipe("2", "Concurrent Product 2"))
        
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode1)).thenReturn(Result.success(recipes1))
        `when`(mockFoodRepository.searchRecipesByBarcode(barcode2)).thenReturn(Result.success(recipes2))

        // When - Start both requests
        viewModel.searchRecipesByBarcode(barcode1)
        viewModel.searchRecipesByBarcode(barcode2)
        
        // Then - Both should be in loading state
        assertTrue(viewModel.isLoading.value)
        
        // When - Advance time to complete both
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Should show results from the last request
        assertEquals(recipes2, viewModel.recipes.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    private fun createTestRecipe(id: String, title: String): HealthyRecipe {
        return HealthyRecipe(
            id = id,
            title = title,
            description = "Test description for $title",
            imageUrl = "https://example.com/test.jpg",
            calories = 200.0,
            protein = 15.0,
            carbs = 25.0,
            fat = 8.0,
            fiber = 3.0,
            servings = 1
        )
    }
}
