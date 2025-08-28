package com.velaphi.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velaphi.core.data.HealthyRecipe
import com.velaphi.core.data.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {
    
    private val _recipes = MutableStateFlow<List<HealthyRecipe>>(emptyList())
    val recipes: StateFlow<List<HealthyRecipe>> = _recipes.asStateFlow()
    
    private val _selectedRecipe = MutableStateFlow<HealthyRecipe?>(null)
    val selectedRecipe: StateFlow<HealthyRecipe?> = _selectedRecipe.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadHealthyRecipes()
    }
    
    fun loadHealthyRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = foodRepository.getHealthyRecipeSuggestions()
                result.fold(
                    onSuccess = { recipes ->
                        _recipes.value = recipes
                        Timber.d("Loaded ${recipes.size} healthy recipes")
                    },
                    onFailure = { exception ->
                        _error.value = "Failed to load recipes: ${exception.message}"
                        Timber.e(exception, "Failed to load recipes")
                    }
                )
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                Timber.e(e, "Unexpected error in loadHealthyRecipes")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchRecipes(query: String) {
        if (query.isBlank()) {
            loadHealthyRecipes()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = foodRepository.searchRecipes(query)
                result.fold(
                    onSuccess = { recipes ->
                        _recipes.value = recipes
                        Timber.d("Search returned ${recipes.size} recipes for query: $query")
                    },
                    onFailure = { exception ->
                        _error.value = "Search failed: ${exception.message}"
                        Timber.e(exception, "Search failed")
                    }
                )
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                Timber.e(e, "Unexpected error in searchRecipes")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectRecipe(recipe: HealthyRecipe) {
        _selectedRecipe.value = recipe
        Timber.d("Selected recipe: ${recipe.title}")
    }
    
    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
        Timber.d("Cleared selected recipe")
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun retryConnection() {
        loadHealthyRecipes()
    }
    
    fun refreshRecipes() {
        loadHealthyRecipes()
    }
    
    fun searchRecipesByBarcode(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = foodRepository.searchRecipesByBarcode(barcode)
                result.fold(
                    onSuccess = { recipes ->
                        _recipes.value = recipes
                        Timber.d("Barcode search returned ${recipes.size} recipes for barcode: $barcode")
                    },
                    onFailure = { exception ->
                        _error.value = "Barcode search failed: ${exception.message}"
                        Timber.e(exception, "Barcode search failed")
                    }
                )
            } catch (e: Exception) {
                _error.value = "Unexpected error in barcode search: ${e.message}"
                Timber.e(e, "Unexpected error in barcode search")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
