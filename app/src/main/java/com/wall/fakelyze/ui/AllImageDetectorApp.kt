package com.wall.fakelyze.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.wall.fakelyze.data.AppContainer
import com.wall.fakelyze.ui.navigation.AIImageDetectorNavHost
import com.wall.fakelyze.ui.navigation.BottomNavigationBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AIImageDetectorApp(appContainer: AppContainer, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { innerPadding ->
        AIImageDetectorNavHost(
            navController = navController,
            appContainer = appContainer,
            modifier = modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        )
    }
}