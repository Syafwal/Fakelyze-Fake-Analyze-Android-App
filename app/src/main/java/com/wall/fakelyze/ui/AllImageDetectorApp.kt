package com.wall.fakelyze.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wall.fakelyze.ui.navigation.AIImageDetectorNavHost
import com.wall.fakelyze.ui.navigation.BottomNavigationBar
import com.wall.fakelyze.ui.navigation.FakelyzeDestination

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AIImageDetectorApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define top level destinations - only show bottom bar for these routes
    val topLevelDestinations = listOf(
        FakelyzeDestination.Home.route,
        FakelyzeDestination.History.route,
        FakelyzeDestination.Info.route,
        FakelyzeDestination.Settings.route
    )

    // Check if current route is a top level destination
    val showBottomBar = currentRoute in topLevelDestinations

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { innerPadding ->
        AIImageDetectorNavHost(
            navController = navController,
            modifier = modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        )
    }
}
