package com.lipo.menu.presentation.todaymenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lipo.menu.presentation.components.DecorativeBanner
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayMenuScreen(
    viewModel: TodayMenuViewModel = hiltViewModel(),
    onNavigateToDishDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (uiState.todayMenu == null && !uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showCreateDialog() },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = { Text("创建今日菜单") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 标题栏
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "今日菜肴 🍽️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = java.time.LocalDate.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("M月d日")
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // 内容
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.todayMenu == null -> {
                        // 空状态
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "还没有创建今日菜单",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击下方按钮开始创建吧！",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    uiState.todayMenu!!.dishes.isEmpty() -> {
                        // 菜单为空
                        DecorativeBanner(
                            message = "今日菜单是空的，快去添加菜品吧！ 🍳",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        // 显示今日菜单
                        TodayMenuSection(
                            todayMenu = uiState.todayMenu,
                            isLoading = uiState.isLoading,
                            onCreateClick = { viewModel.showCreateDialog() },
                            onUpdateClick = { viewModel.showUpdateDialog() },
                            onDeleteClick = { viewModel.showDeleteDialog() },
                            onDishClick = onNavigateToDishDetail,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // 错误信息
                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("关闭")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }

        // 对话框
        if (uiState.showCreateDialog) {
            CreateTodayMenuDialog(
                allDishes = uiState.allDishes,
                allCombinations = uiState.allCombinations,
                onDismiss = { viewModel.hideCreateDialog() },
                onConfirm = { date, dishIds ->
                    viewModel.createTodayMenu(date, dishIds)
                }
            )
        }

        uiState.showUpdateDialog?.let { menu ->
            UpdateTodayMenuDialog(
                todayMenu = menu,
                allDishes = uiState.allDishes,
                onDismiss = { viewModel.hideUpdateDialog() },
                onConfirm = { id, date, dishIds ->
                    viewModel.updateTodayMenu(id, date, dishIds)
                }
            )
        }

        uiState.showDeleteDialog?.let { menu ->
            DeleteTodayMenuDialog(
                onDismiss = { viewModel.hideDeleteDialog() },
                onConfirm = {
                    viewModel.deleteTodayMenu(menu.id)
                }
            )
        }
    }
}
