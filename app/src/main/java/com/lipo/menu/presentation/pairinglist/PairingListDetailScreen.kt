package com.lipo.menu.presentation.pairinglist

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lipo.menu.data.model.Dish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingListDetailScreen(
    combinationId: String,
    onNavigateBack: () -> Unit,
    viewModel: PairingListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(combinationId) {
        viewModel.loadCombination(combinationId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PairingListDetailTopBar(
                title = uiState.combination?.name ?: "搭配详情",
                onNavigateBack = onNavigateBack,
                onEdit = { viewModel.showEditDialog() }
            )
        },
        floatingActionButton = {
            if (uiState.combination != null) {
                PairingListDetailFAB(
                    onClick = { viewModel.showAddDishDialog() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    PairingListLoadingState()
                }

                uiState.combination == null -> {
                    PairListNotFoundError()
                }

                else -> {
                    val combination = uiState.combination!!

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Description section
                        combination.description?.let { desc ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📝",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "描述",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Dishes list
                        if (combination.dishes.isEmpty()) {
                            PairingListEmptyDishesState()
                        } else {
                            PairingListDishesList(
                                dishes = combination.dishes,
                                onRemoveDish = { dish -> viewModel.showRemoveDishDialog(dish) }
                            )
                        }
                    }
                }
            }

            // Error message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("关闭")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(error)
                }
            }
        }
    }

    // Dialogs
    if (uiState.showEditDialog) {
        uiState.combination?.let { combination ->
            EditPairingListDialog(
                initialName = combination.name,
                initialDescription = combination.description,
                onDismiss = { viewModel.hideEditDialog() },
                onUpdate = { name, description ->
                    viewModel.updateCombination(combination.id, name, description)
                }
            )
        }
    }

    if (uiState.showAddDishDialog) {
        AddDishToPairingListDialog(
            availableDishes = uiState.availableDishes,
            onDismiss = { viewModel.hideAddDishDialog() },
            onAdd = { dishId ->
                uiState.combination?.let { combination ->
                    viewModel.addDishToCombination(combination.id, dishId)
                }
            }
        )
    }

    uiState.showRemoveDishDialog?.let { dish ->
        uiState.combination?.let { combination ->
            RemoveDishFromPairingDialog(
                dishName = dish.name,
                isLastDish = combination.dishes.size <= 1,
                onDismiss = { viewModel.hideRemoveDishDialog() },
                onConfirm = {
                    viewModel.removeDishFromCombination(combination.id, dish.id)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingListDetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun PairListNotFoundError() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "搭配清单未找到",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PairingListEmptyDishesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "bounce")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Text(
            text = "🍽️",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "这个搭配清单还没有菜品",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "点击右下角的按钮添加菜品吧！💕",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PairingListDishesList(
    dishes: List<Dish>,
    onRemoveDish: (Dish) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = dishes,
            key = { it.id }
        ) { dish ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                PairingListDishCard(
                    dish = dish,
                    onRemove = { onRemoveDish(dish) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingListDishCard(
    dish: Dish,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left decorative icon
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                color = getRandomPastelColor(dish.id)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getRandomFoodEmoji(dish.id),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                dish.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Remove button
            FilledIconButton(
                onClick = onRemove,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "移除",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PairingListDetailFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        text = {
            Text(
                "添加菜品",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

// Helper functions for random colors and emojis
private fun getRandomPastelColor(id: String): Color {
    val colors = listOf(
        Color(0xFFFFE5E5), // Light pink
        Color(0xFFE5F0FF), // Light blue
        Color(0xFFE5FFE5), // Light green
        Color(0xFFFFF5E5), // Light orange
        Color(0xFFF0E5FF), // Light purple
        Color(0xFFFFE5F0), // Light rose
        Color(0xFFE5FFFF), // Light cyan
        Color(0xFFFFF0E5)  // Light brown
    )
    val index = Math.abs(id.hashCode()) % colors.size
    return colors[index]
}

private fun getRandomFoodEmoji(id: String): String {
    val emojis = listOf("🍳", "🥗", "🍝", "🍜", "🍰", "🥘", "🍲", "🍱", "🥙", "🌮")
    val index = Math.abs(id.hashCode()) % emojis.size
    return emojis[index]
}
