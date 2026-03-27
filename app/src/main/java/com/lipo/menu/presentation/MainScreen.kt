package com.lipo.menu.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lipo.menu.presentation.navigation.MenuNavGraph
import com.lipo.menu.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // 今日菜肴 - 主页
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("今日") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate(Screen.TodayMenu.route) {
                            popUpTo(Screen.TodayMenu.route) { inclusive = true }
                        }
                    }
                )

                // 菜品列表
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("菜品") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate(Screen.DishList.route) {
                            popUpTo(Screen.DishList.route) { inclusive = true }
                        }
                    }
                )

                // 搭配
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("搭配") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate(Screen.PairingList.route) {
                            popUpTo(Screen.PairingList.route) { inclusive = true }
                        }
                    }
                )

                // 历史
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("历史") },
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.History.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MenuNavGraph(
                navController = navController,
                startDestination = when (selectedTab) {
                    0 -> Screen.TodayMenu.route
                    1 -> Screen.DishList.route
                    2 -> Screen.PairingList.route
                    else -> Screen.History.route
                }
            )
        }
    }
}
