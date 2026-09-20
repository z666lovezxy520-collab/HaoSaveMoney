package com.hao.savemoney.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hao.savemoney.ui.dashboard.DashboardScreen
import com.hao.savemoney.ui.dashboard.DashboardViewModel
import com.hao.savemoney.ui.export.ExportScreen
import com.hao.savemoney.ui.export.ExportViewModel
import com.hao.savemoney.ui.home.HomeScreen
import com.hao.savemoney.ui.home.HomeViewModel
import com.hao.savemoney.ui.income.IncomeSetupScreen
import com.hao.savemoney.ui.income.IncomeViewModel

/**
 * 应用导航路由
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Dashboard : Screen("dashboard", "总览", Icons.Default.BarChart)
    data object Export : Screen("export", "导出", Icons.Default.Upload)
    data object IncomeSetup : Screen("income_setup", "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Dashboard, Screen.Export)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 收入设置页不显示底部导航栏
    val showBottomBar = currentRoute != Screen.IncomeSetup.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToIncome = {
                        navController.navigate(Screen.IncomeSetup.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.IncomeSetup.route) {
                val viewModel: IncomeViewModel = hiltViewModel()
                IncomeSetupScreen(
                    viewModel = viewModel,
                    onSaveComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(viewModel = viewModel)
            }

            composable(Screen.Export.route) {
                val viewModel: ExportViewModel = hiltViewModel()
                ExportScreen(viewModel = viewModel)
            }
        }
    }
}
