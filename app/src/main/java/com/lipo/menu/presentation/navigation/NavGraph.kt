package com.lipo.menu.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lipo.menu.presentation.dish.DishDetailScreen
import com.lipo.menu.presentation.dish.DishListScreen
import com.lipo.menu.presentation.dish.DishDetailViewModel
import com.lipo.menu.presentation.pairinglist.PairingListScreen
import com.lipo.menu.presentation.pairinglist.PairingListDetailScreen
import com.lipo.menu.presentation.history.MenuHistoryScreen
import com.lipo.menu.presentation.history.MenuHistoryDetailScreen
import com.lipo.menu.presentation.history.MenuHistoryDetailViewModel
import com.lipo.menu.presentation.todaymenu.TodayMenuScreen

sealed class Screen(val route: String) {
    object DishList : Screen("dish_list")
    object DishDetail : Screen("dish_detail/{dishId}") {
        fun createRoute(dishId: String) = "dish_detail/$dishId"
    }
    object PairingList : Screen("pairing_list")
    object PairingListDetail : Screen("pairing_list_detail/{combinationId}") {
        fun createRoute(combinationId: String) = "pairing_list_detail/$combinationId"
    }
    object TodayMenu : Screen("today_menu")
    object History : Screen("history")
    object HistoryDetail : Screen("history_detail/{menuId}") {
        fun createRoute(menuId: String) = "history_detail/$menuId"
    }
    object Combinations : Screen("combinations")
}

@Composable
fun MenuNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.DishList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.DishList.route) {
            DishListScreen(
                onNavigateToDetail = { dishId ->
                    navController.navigate(Screen.DishDetail.createRoute(dishId))
                }
            )
        }

        composable(
            route = Screen.DishDetail.route,
            arguments = listOf(
                navArgument("dishId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dishId = backStackEntry.arguments?.getString("dishId") ?: return@composable Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: Dish ID not found")
            }

            DishDetailRoute(
                dishId = dishId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.popBackStack() }
            )
        }

        composable(Screen.TodayMenu.route) {
            TodayMenuScreen(
                onNavigateToDishDetail = { dishId ->
                    navController.navigate(Screen.DishDetail.createRoute(dishId))
                }
            )
        }

        composable(Screen.History.route) {
            MenuHistoryScreen(
                onNavigateToDetail = { menuId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(menuId))
                }
            )
        }

        composable(
            route = Screen.HistoryDetail.route,
            arguments = listOf(
                navArgument("menuId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val menuId = backStackEntry.arguments?.getString("menuId") ?: return@composable Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: Menu ID not found")
            }

            MenuHistoryDetailRoute(
                menuId = menuId,
                onBack = { navController.popBackStack() },
                onNavigateToDishDetail = { dishId ->
                    navController.navigate(Screen.DishDetail.createRoute(dishId))
                }
            )
        }

        composable(Screen.Combinations.route) {
            // TODO: Add CombinationsScreen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Combinations Screen - Coming Soon")
            }
        }

        composable(Screen.PairingList.route) {
            PairingListScreen(
                onNavigateToDetail = { combinationId ->
                    navController.navigate(Screen.PairingListDetail.createRoute(combinationId))
                }
            )
        }

        composable(
            route = Screen.PairingListDetail.route,
            arguments = listOf(
                navArgument("combinationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val combinationId = backStackEntry.arguments?.getString("combinationId") ?: return@composable Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: Combination ID not found")
            }

            PairingListDetailScreen(
                combinationId = combinationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DishDetailRoute(
    dishId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: DishDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(dishId) {
        viewModel.loadDish(dishId)
    }

    uiState.dish?.let { dish ->
        DishDetailScreen(
            dish = dish,
            onEdit = {
                viewModel.showEditDialog()
            },
            onDelete = {
                viewModel.showDeleteDialog()
            },
            onBack = onBack,
            showDeleteDialog = uiState.showDeleteDialog,
            onDismissDeleteDialog = { viewModel.hideDeleteDialog() },
            onConfirmDelete = {
                viewModel.deleteDish(dish.id)
                onBack()
            },
            combinationCount = uiState.combinationCount,
            usageCount = uiState.usageCount
        )
    }

    // 编辑对话框
    uiState.showEditDialog?.let { dish ->
        com.lipo.menu.presentation.dish.EditDishDialog(
            dish = dish,
            onDismiss = { viewModel.hideEditDialog() },
            onUpdate = { name, description ->
                viewModel.updateDish(dish.id, name, description)
            }
        )
    }
}

@Composable
fun MenuHistoryDetailRoute(
    menuId: String,
    onBack: () -> Unit,
    onNavigateToDishDetail: (String) -> Unit,
    viewModel: MenuHistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(menuId) {
        viewModel.loadMenu(menuId)
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            MenuHistoryDetailScreen(
                menu = uiState.menu,
                onBack = onBack,
                onDishClick = onNavigateToDishDetail
            )
        }
    }
}
