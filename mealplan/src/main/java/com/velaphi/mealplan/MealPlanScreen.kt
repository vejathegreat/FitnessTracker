package com.velaphi.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.velaphi.core.data.HealthyRecipe
import androidx.compose.ui.res.stringResource

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchAndBarcodeSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    if (query.isBlank()) {
                        viewModel.loadHealthyRecipes()
                    } else {
                        viewModel.searchRecipes(query)
                    }
                },
                onBarcodeScanClick = { showBarcodeScanner = true }
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.weight(1f)) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.weight(1f)) {
                        ErrorState(message = error!!, onRetry = { viewModel.retryConnection() })
                    }
                }
                recipes.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(recipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { viewModel.selectRecipe(recipe) }
                            )
                        }
                    }
                }
                else -> {
                    // No recipes state
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No recipes available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refreshRecipes() }) {
                                Text("Refresh")
                            }
                        }
                    }
                }
            }
        }

        // Recipe Details Dialog
        selectedRecipe?.let { recipe ->
            RecipeDetailsDialog(
                recipe = recipe,
                onDismiss = { viewModel.clearSelectedRecipe() }
            )
        }
        
        // Barcode Scanner Dialog
        if (showBarcodeScanner) {
            BarcodeScannerDialog(
                onBarcodeDetected = { barcode ->
                    showBarcodeScanner = false
                    viewModel.searchRecipesByBarcode(barcode)
                },
                onError = { errorMessage ->
                    showBarcodeScanner = false
                    // Error will be handled by the ViewModel
                },
                onDismiss = { showBarcodeScanner = false }
            )
        }
    }
}

@Composable
fun SearchAndBarcodeSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBarcodeScanClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.search_recipes_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        IconButton(
            onClick = onBarcodeScanClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.scan_barcode),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BarcodeScannerDialog(
    onBarcodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.scan_barcode_title)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                BarcodeScanner(
                    onBarcodeDetected = onBarcodeDetected,
                    onError = onError,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecipeCard(
    recipe: HealthyRecipe,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp), // Reduced height for more compact cards
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.clickable { onClick() }
        ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Recipe Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Calories: ${recipe.calories.toInt()} | Protein: ${recipe.protein.toInt()}g",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Carbs: ${recipe.carbs.toInt()}g | Fat: ${recipe.fat.toInt()}g",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        }
    }
}

@Composable
fun RecipeDetailsDialog(
    recipe: HealthyRecipe,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                item {
                    // Recipe Image
                    if (!recipe.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(recipe.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Recipe: ${recipe.title}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Recipe",
                                tint = Color.Gray,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    recipe.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Nutrition Details
                    Text(
                        text = "Nutrition Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NutritionRow("Calories", "${recipe.calories.toInt()}")
                        NutritionRow("Protein", "${recipe.protein.toInt()}g")
                        NutritionRow("Carbohydrates", "${recipe.carbs.toInt()}g")
                        NutritionRow("Fat", "${recipe.fat.toInt()}g")
                        NutritionRow("Fiber", "${recipe.fiber.toInt()}g")
                        NutritionRow("Servings", "${recipe.servings.toInt()}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Additional Info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        recipe.prepTime.takeIf { it > 0 }?.let { time ->
                            InfoChip(
                                icon = Icons.Default.Star,
                                label = "Prep: ${time.toInt()}min"
                            )
                        }
                        recipe.cookTime.takeIf { it > 0 }?.let { time ->
                            InfoChip(
                                icon = Icons.Default.Star,
                                label = "Cook: ${time.toInt()}min"
                            )
                        }
                        recipe.difficulty?.let { diff ->
                            InfoChip(
                                icon = Icons.Default.Star,
                                label = diff
                            )
                        }
                    }

                    // Source and URL
                    recipe.originalUrl?.let { url ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Source: ${recipe.source}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NutritionRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Star, contentDescription = "Retry")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
@Composable
fun EmptyState(
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = "No recipes",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No recipes found",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRefresh) {
            Icon(Icons.Default.Star, contentDescription = "Refresh")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
} 
