package com.velaphi.core.data

import com.velaphi.core.data.model.HealthyRecipe
import com.velaphi.core.data.repository.TheMealDBRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class FoodRepositoryTest {

    @Mock
    private lateinit var mockTheMealDBRepository: TheMealDBRepository

    private lateinit var foodRepository: FoodRepository

    @Before
    fun setUp() {
        foodRepository = FoodRepository(mockTheMealDBRepository)
    }

    @Test
    fun `searchRecipes should delegate to TheMealDBRepository`() = runTest {
        // Given
        val query = "chicken"
        val expectedRecipes = listOf(
            HealthyRecipe(
                id = "1",
                title = "Grilled Chicken",
                description = "Healthy grilled chicken recipe",
                imageUrl = "https://example.com/chicken.jpg",
                calories = 250.0,
                protein = 30.0,
                carbs = 5.0,
                fat = 12.0,
                fiber = 2.0,
                servings = 2
            )
        )
        `when`(mockTheMealDBRepository.searchHealthyRecipes(query)).thenReturn(Result.success(expectedRecipes))

        // When
        val result = foodRepository.searchRecipes(query)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipes, result.getOrNull())
        verify(mockTheMealDBRepository).searchHealthyRecipes(query)
    }

    @Test
    fun `searchRecipes should return failure when TheMealDBRepository fails`() = runTest {
        // Given
        val query = "invalid"
        val expectedError = Exception("Network error")
        `when`(mockTheMealDBRepository.searchHealthyRecipes(query)).thenReturn(Result.failure(expectedError))

        // When
        val result = foodRepository.searchRecipes(query)

        // Then
        assertFalse(result.isSuccess)
        assertEquals(expectedError, result.exceptionOrNull())
        verify(mockTheMealDBRepository).searchHealthyRecipes(query)
    }

    @Test
    fun `getHealthyRecipeSuggestions should delegate to TheMealDBRepository`() = runTest {
        // Given
        val expectedRecipes = listOf(
            HealthyRecipe(
                id = "1",
                title = "Healthy Salad",
                description = "Fresh vegetable salad",
                imageUrl = "https://example.com/salad.jpg",
                calories = 150.0,
                protein = 8.0,
                carbs = 20.0,
                fat = 6.0,
                fiber = 5.0,
                servings = 1
            )
        )
        `when`(mockTheMealDBRepository.getRandomRecipes(10)).thenReturn(Result.success(expectedRecipes))

        // When
        val result = foodRepository.getHealthyRecipeSuggestions()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipes, result.getOrNull())
        verify(mockTheMealDBRepository).getRandomRecipes(10)
    }

    @Test
    fun `getHealthyRecipeSuggestions should return failure when TheMealDBRepository fails`() = runTest {
        // Given
        val expectedError = Exception("Database error")
        `when`(mockTheMealDBRepository.getRandomRecipes(10)).thenReturn(Result.failure(expectedError))

        // When
        val result = foodRepository.getHealthyRecipeSuggestions()

        // Then
        assertFalse(result.isSuccess)
        assertEquals(expectedError, result.exceptionOrNull())
        verify(mockTheMealDBRepository).getRandomRecipes(10)
    }

    @Test
    fun `searchRecipesByBarcode should delegate to TheMealDBRepository`() = runTest {
        // Given
        val barcode = "1234567890123"
        val expectedRecipes = listOf(
            HealthyRecipe(
                id = "1",
                title = "Barcode Product",
                description = "Product found by barcode",
                imageUrl = "https://example.com/product.jpg",
                calories = 200.0,
                protein = 15.0,
                carbs = 25.0,
                fat = 8.0,
                fiber = 3.0,
                servings = 1
            )
        )
        `when`(mockTheMealDBRepository.searchHealthyRecipes(barcode)).thenReturn(Result.success(expectedRecipes))

        // When
        val result = foodRepository.searchRecipesByBarcode(barcode)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipes, result.getOrNull())
        verify(mockTheMealDBRepository).searchHealthyRecipes(barcode)
    }

    @Test
    fun `searchRecipesByBarcode should return failure when TheMealDBRepository fails`() = runTest {
        // Given
        val barcode = "invalid_barcode"
        val expectedError = Exception("Barcode not found")
        `when`(mockTheMealDBRepository.searchHealthyRecipes(barcode)).thenReturn(Result.failure(expectedError))

        // When
        val result = foodRepository.searchRecipesByBarcode(barcode)

        // Then
        assertFalse(result.isSuccess)
        assertEquals(expectedError, result.exceptionOrNull())
        verify(mockTheMealDBRepository).searchHealthyRecipes(barcode)
    }

    @Test
    fun `searchRecipesByBarcode should handle empty barcode gracefully`() = runTest {
        // Given
        val barcode = ""
        val expectedRecipes = emptyList<HealthyRecipe>()
        `when`(mockTheMealDBRepository.searchHealthyRecipes(barcode)).thenReturn(Result.success(expectedRecipes))

        // When
        val result = foodRepository.searchRecipesByBarcode(barcode)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipes, result.getOrNull())
        verify(mockTheMealDBRepository).searchHealthyRecipes(barcode)
    }

    @Test
    fun `searchRecipesByBarcode should handle special characters in barcode`() = runTest {
        // Given
        val barcode = "ABC-123_456"
        val expectedRecipes = listOf(
            HealthyRecipe(
                id = "1",
                title = "Special Barcode Product",
                description = "Product with special characters",
                imageUrl = "https://example.com/special.jpg",
                calories = 180.0,
                protein = 12.0,
                carbs = 22.0,
                fat = 7.0,
                fiber = 4.0,
                servings = 1
            )
        )
        `when`(mockTheMealDBRepository.searchHealthyRecipes(barcode)).thenReturn(Result.success(expectedRecipes))

        // When
        val result = foodRepository.searchRecipesByBarcode(barcode)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipes, result.getOrNull())
        verify(mockTheMealDBRepository).searchHealthyRecipes(barcode)
    }
}
