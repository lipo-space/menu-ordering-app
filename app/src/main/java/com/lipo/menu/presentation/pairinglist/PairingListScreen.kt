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
import com.lipo.menu.data.model.Combination
import com.lipo.menu.presentation.dish.CuteSearchBar
import com.lipo.menu.presentation.dish.DecorativeBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingListScreen(
    viewModel: PairingListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PairingListTopBar(
                title = "搭配清单 🍱",
                subtitle = "共 ${uiState.combinations.size} 个搭配"
            )
        },
        floatingActionButton = {
            PairingListFAB(
                onClick = { viewModel.showCreateDialog() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            CuteSearchBar(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Decorative banner
            if (uiState.combinations.isNotEmpty()) {
                DecorativeBanner(
                    message = "保存你喜爱的菜品搭配 💝",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Content area
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    // Loading
                    uiState.isLoading -> {
                        PairingListLoadingState()
                    }

                    // Empty state
                    uiState.combinations.isEmpty() && !uiState.isLoading -> {
                        PairingListEmptyState(
                            searchQuery = uiState.searchQuery
                        )
                    }

                    // List
                    else -> {
                        PairingListGridList(
                            combinations = uiState.combinations,
                            onItemClick = { combinationId -> onNavigateToDetail(combinationId) },
                            onEdit = { combination -> viewModel.showEditDialog(combination) },
                            onDelete = { combination -> viewModel.showDeleteDialog(combination) }
                        )
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
        if (uiState.showCreateDialog) {
            CreatePairingListDialog(
                availableDishes = uiState.availableDishes,
                onDismiss = { viewModel.hideCreateDialog() },
                onCreate = { name, description, dishIds ->
                    viewModel.createCombination(name, description, dishIds)
                }
            )
        }

        uiState.showEditDialog?.let { combination ->
            EditPairingListDialog(
                initialName = combination.name,
                initialDescription = combination.description,
                onDismiss = { viewModel.hideEditDialog() },
                onUpdate = { name, description ->
                    viewModel.updateCombination(combination.id, name, description)
                }
            )
        }

        uiState.showDeleteDialog?.let { combination ->
            DeletePairingListDialog(
                pairingListName = combination.name,
                onDismiss = { viewModel.hideDeleteDialog() },
                onConfirm = {
                    viewModel.deleteCombination(combination.id)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingListTopBar(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PairingListLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "加载中... 🍳",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PairingListEmptyState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated emoji
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
            text = if (searchQuery.isEmpty()) "🍱" else "🤔",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (searchQuery.isEmpty()) "还没有搭配清单哦～" else "没有找到匹配的搭配清单",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (searchQuery.isEmpty()) "快点击右下角的按钮\n创建第一个搭配吧！💕" else "试试其他搜索词吧～",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            // Decorative emojis
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DecorativeEmoji("🥗")
                DecorativeEmoji("🍝")
                DecorativeEmoji("🍰")
            }
        }
    }
}

@Composable
fun DecorativeEmoji(emoji: String) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun PairingListGridList(
    combinations: List<Combination>,
    onItemClick: (String) -> Unit,
    onEdit: (Combination) -> Unit,
    onDelete: (Combination) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = combinations,
            key = { it.id }
        ) { combination ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                CutePairingListCard(
                    combination = combination,
                    onClick = { onItemClick(combination.id) },
                    onEdit = { onEdit(combination) },
                    onDelete = { onDelete(combination) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutePairingListCard(
    combination: Combination,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
        ),
        onClick = onClick
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
                color = getRandomPastelColor(combination.id)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getRandomCombinationEmoji(combination.id),
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
                    text = combination.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                combination.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dish count badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${combination.dishes.size} 道菜",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button
                FilledIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                FilledIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PairingListFAB(
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
                "创建搭配",
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

private fun getRandomCombinationEmoji(id: String): String {
    val emojis = listOf("🍱", "🥘", "🍲", "🍽️", "🥗", "🥙", "🥪", "🌮", "🥡", "🍱")
    val index = Math.abs(id.hashCode()) % emojis.size
    return emojis[index]
}
