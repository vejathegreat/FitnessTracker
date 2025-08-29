package com.velaphi.core.data

import com.velaphi.core.constants.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class TheMealDBRepository {

    private val httpClient = OkHttpClient()

    init {
        debugLog("Repository initialized with baseUrl=${AppConstants.THEMEALDB_BASE_URL}")
    }

    suspend fun searchHealthyRecipes(
        query: String = "chicken",
        maxResults: Int = AppConstants.DEFAULT_MAX_RESULTS,
        pageNumber: Int = AppConstants.DEFAULT_PAGE_NUMBER
    ): Result<List<HealthyRecipe>> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "${AppConstants.THEMEALDB_BASE_URL}${AppConstants.RECIPE_SEARCH_ENDPOINT}?s=$encodedQuery"
        debugLog("Searching recipes: $url")

        executeRequest(url) { body -> parseTheMealDBResponse(body) }
    }

    suspend fun getRandomRecipes(
        count: Int = 80
    ): Result<List<HealthyRecipe>> = withContext(Dispatchers.IO) {
        try {
            val recipes = mutableListOf<HealthyRecipe>()

            repeat(count) {
                val url = "${AppConstants.THEMEALDB_BASE_URL}${AppConstants.RECIPE_RANDOM_ENDPOINT}"
                executeRequest(url) { body -> parseTheMealDBResponse(body) }
                    .onSuccess { recipes.addAll(it) }
            }
            Result.success(recipes.distinctBy { it.id }) // avoid duplicates
        } catch (e: Exception) {
            debugLog("Exception in getRandomRecipes: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseTheMealDBResponse(responseBody: String): Result<List<HealthyRecipe>> = runCatching {
        val mealsArray = JSONObject(responseBody).optJSONArray("meals") ?: return Result.success(emptyList())
        val recipes = (0 until mealsArray.length())
            .mapNotNull { idx -> mealsArray.optJSONObject(idx)?.let { parseMealFromJson(it) } }

        debugLog("Parsed ${recipes.size} recipes")
        recipes
    }

    private fun parseCategoriesResponse(responseBody: String): Result<List<TheMealDBCategory>> = runCatching {
        val categoriesArray = JSONObject(responseBody).optJSONArray("categories") ?: return Result.success(emptyList())
        val categories = (0 until categoriesArray.length())
            .mapNotNull { idx ->
                categoriesArray.optJSONObject(idx)?.let { cat ->
                    TheMealDBCategory(
                        idCategory = cat.optString("idCategory"),
                        strCategory = cat.optString("strCategory"),
                        strCategoryThumb = cat.optString("strCategoryThumb"),
                        strCategoryDescription = cat.optString("strCategoryDescription")
                    )
                }
            }
        debugLog("Parsed ${categories.size} categories")
        categories
    }

    private fun parseMealFromJson(mealJson: JSONObject): HealthyRecipe {
        val ingredients = (1..20).mapNotNull { i ->
            mealJson.optString("strIngredient$i").takeIf { it.isNotBlank() && it != "null" }
        }

        val measures = (1..20).mapNotNull { i ->
            mealJson.optString("strMeasure$i").takeIf { it.isNotBlank() && it != "null" }
        }

        val ingredientList = ingredients.mapIndexed { idx, ingredient ->
            measures.getOrNull(idx)?.takeIf { it.isNotBlank() }?.let { "$it $ingredient" } ?: ingredient
        }

        return HealthyRecipe(
            id = mealJson.optString("idMeal"),
            title = mealJson.optString("strMeal"),
            description = mealJson.optString("strInstructions").take(200) + "...",
            imageUrl = mealJson.optString("strMealThumb"),
            source = mealJson.optString("strSource").ifBlank { "TheMealDB" },
            calories = ingredients.size * 50.0, // crude estimate
            protein = ingredients.count { it.contains("chicken|fish|meat".toRegex(RegexOption.IGNORE_CASE)) } * 25.0,
            carbs = ingredients.count { it.contains("rice|pasta|bread".toRegex(RegexOption.IGNORE_CASE)) } * 30.0,
            fat = ingredients.count { it.contains("oil|butter|cheese".toRegex(RegexOption.IGNORE_CASE)) } * 15.0,
            fiber = 5.0,
            servings = 4.0,
            prepTime = 15.0,
            cookTime = 30.0,
            difficulty = "Medium",
            cuisine = mealJson.optString("strArea"),
            course = mealJson.optString("strCategory"),
            healthLabels = listOf("Healthy", "Balanced"),
            originalUrl = mealJson.optString("strSource"),
        )
    }

    private fun <T> executeRequest(
        url: String,
        parser: (String) -> Result<T>
    ): Result<T> = try {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("Request failed [${response.code}]: ${response.message}"))
            }
            val body = response.body?.string() ?: return Result.failure(Exception("Empty response body"))
            parser(body)
        }
    } catch (e: Exception) {
        debugLog("Request failed: ${e.message}", e)
        Result.failure(e)
    }

    private fun debugLog(message: String, e: Throwable? = null) {
        println("DEBUG: $message")
        e?.printStackTrace()
    }
}
