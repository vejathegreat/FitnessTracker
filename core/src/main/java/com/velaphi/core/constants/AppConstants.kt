package com.velaphi.core.constants

object AppConstants {
    // TheMealDB Recipe Search API Configuration - Free API, no authentication required
    // API Documentation: https://www.themealdb.com/api.php
    const val THEMEALDB_BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    
    // API Endpoints
    const val RECIPE_SEARCH_ENDPOINT = "search.php"
    const val RECIPE_GET_ENDPOINT = "lookup.php"
    const val RECIPE_CATEGORIES_ENDPOINT = "categories.php"
    const val RECIPE_RANDOM_ENDPOINT = "random.php"
    const val RECIPE_BY_CATEGORY_ENDPOINT = "filter.php"
    
    // API parameters
    const val DEFAULT_MAX_RESULTS = 20
    const val MAX_RESULTS = 50
    const val DEFAULT_PAGE_NUMBER = 0
    
    // Search terms for healthy foods
    val HEALTHY_FOOD_SEARCH_TERMS = listOf(
        "chicken",
        "salmon",
        "quinoa",
        "avocado",
        "spinach",
        "berries",
        "nuts",
        "yogurt",
        "eggs",
        "oatmeal"
    )
    
    // Food categories
    val FOOD_CATEGORIES = listOf(
        "Breakfast",
        "Lunch", 
        "Main Course",
        "Salad",
        "Snack",
        "Dessert",
        "Beverage"
    )
    
    // Nutrition thresholds for healthy foods
    const val HEALTHY_CALORIES_THRESHOLD = 400
    const val HIGH_PROTEIN_THRESHOLD = 20
    const val HIGH_FIBER_THRESHOLD = 8
    const val LOW_FAT_THRESHOLD = 15
}
