package com.velaphi.core.data

import javax.inject.Inject

class FoodRepository @Inject constructor(
    private val theMealDBRepository: TheMealDBRepository
) {
    
    suspend fun searchRecipes(query: String): Result<List<HealthyRecipe>> {
        return theMealDBRepository.searchHealthyRecipes(query)
    }
    
    suspend fun getHealthyRecipeSuggestions(): Result<List<HealthyRecipe>> {
        return theMealDBRepository.getRandomRecipes(10)
    }
    
    suspend fun searchRecipesByBarcode(barcode: String): Result<List<HealthyRecipe>> {
        // For now, we'll search by barcode as a query
        // In a real app, you might have a barcode-to-food database
        return theMealDBRepository.searchHealthyRecipes(barcode)
    }
}
