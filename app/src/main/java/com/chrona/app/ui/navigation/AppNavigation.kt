package com.chrona.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.chrona.app.R
import com.chrona.app.ui.alarm.AlarmScreen
import com.chrona.app.ui.clock.ClockScreen
import com.chrona.app.ui.stopwatch.StopwatchScreen
import com.chrona.app.ui.timer.*

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Clock, R.string.nav_clock, Icons.Default.Schedule),
    BottomNavItem(Screen.Alarm, R.string.nav_alarm, Icons.Default.Alarm),
    BottomNavItem(Screen.Stopwatch, R.string.nav_stopwatch, Icons.Default.Timer),
    BottomNavItem(Screen.TimerList, R.string.nav_timer, Icons.Default.FitnessCenter)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    val showBottomBar = remember(currentDestination?.route) {
        currentDestination?.route in setOf(
            Screen.Clock.route,
            Screen.Alarm.route,
            Screen.Stopwatch.route,
            Screen.TimerList.route
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
            startDestination = Screen.Clock.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Clock.route) {
                ClockScreen()
            }
            composable(Screen.Alarm.route) {
                AlarmScreen(
                    onNavigateToEdit = { /* TODO: alarm edit */ }
                )
            }
            composable(Screen.Stopwatch.route) {
                StopwatchScreen()
            }
            composable(Screen.TimerList.route) {
                TimerListScreen(
                    onTimerClick = { id ->
                        navController.navigate(Screen.TimerDetail(id).buildRoute())
                    },
                    onNewTimer = {
                        navController.navigate(Screen.TimerNew.route)
                    }
                )
            }
            // Timer New
            composable(Screen.TimerNew.route) {
                TimerEditScreen(
                    timerId = null,
                    onSaved = { id ->
                        navController.navigate(Screen.TimerDetail(id).buildRoute()) {
                            popUpTo(Screen.TimerList.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            // Timer Edit
            composable(
                route = Screen.TimerEdit.ROUTE,
                arguments = listOf(navArgument(Screen.TimerEdit.ARG) { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong(Screen.TimerEdit.ARG) ?: 0L
                TimerEditScreen(
                    timerId = id,
                    onSaved = { savedId ->
                        navController.navigate(Screen.TimerDetail(savedId).buildRoute()) {
                            popUpTo(Screen.TimerList.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            // Timer Detail
            composable(
                route = Screen.TimerDetail.ROUTE,
                arguments = listOf(navArgument(Screen.TimerDetail.ARG) { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong(Screen.TimerDetail.ARG) ?: 0L
                TimerDetailScreen(
                    timerId = id,
                    onEdit = { navController.navigate(Screen.TimerEdit(id).buildRoute()) },
                    onStart = { navController.navigate(Screen.TimerRunning(id).buildRoute()) },
                    onBack = { navController.popBackStack() }
                )
            }
            // Timer Running
            composable(
                route = Screen.TimerRunning.ROUTE,
                arguments = listOf(navArgument(Screen.TimerRunning.ARG) { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong(Screen.TimerRunning.ARG) ?: 0L
                TimerRunningScreen(
                    timerId = id,
                    onFinished = { navController.popBackStack(Screen.TimerList.route, false) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
