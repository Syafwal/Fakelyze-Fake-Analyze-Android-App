package com.wall.fakelyze.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wall.fakelyze.data.AppContainer
import com.wall.fakelyze.ui.screens.details.DetailsScreen
import com.wall.fakelyze.ui.screens.history.HistoryScreen
import com.wall.fakelyze.ui.screens.history.HistoryViewModel
import com.wall.fakelyze.ui.screens.home.HomeScreen
import com.wall.fakelyze.ui.screens.home.HomeViewModel
import com.wall.fakelyze.ui.screens.learn.LearnScreen
import com.wall.fakelyze.ui.screens.learn.LearnViewModel
import com.wall.fakelyze.ui.screens.results.ResultsScreen
import com.wall.fakelyze.ui.screens.settings.SettingsScreen
import com.wall.fakelyze.ui.screens.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AIImageDetectorNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToResults = { imagePath, thumbnailPath, isAIGenerated, confidenceScore ->
                    navController.navigate(
                        "results?imagePath=$imagePath&thumbnailPath=$thumbnailPath&isAIGenerated=$isAIGenerated&confidenceScore=$confidenceScore"
                    )
                },
                viewModel = koinViewModel<HomeViewModel>()
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = koinViewModel<HistoryViewModel>(),
                onItemClick = { detectionResultId ->
                    navController.navigate("details/$detectionResultId")
                }
            )
        }

        composable("learn") {
            LearnScreen(
                viewModel = koinViewModel<LearnViewModel>(),
                appContainer = appContainer
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = koinViewModel<SettingsViewModel>,
                userPreferencesRepository = appContainer.userPreferencesRepository
            )
        }

        composable(
            route = "results?imagePath={imagePath}&thumbnailPath={thumbnailPath}&isAIGenerated={isAIGenerated}&confidenceScore={confidenceScore}",
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType },
                navArgument("thumbnailPath") { type = NavType.StringType },
                navArgument("isAIGenerated") { type = NavType.BoolType },
                navArgument("confidenceScore") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val thumbnailPath = backStackEntry.arguments?.getString("thumbnailPath") ?: ""
            val isAIGenerated = backStackEntry.arguments?.getBoolean("isAIGenerated") ?: false
            val confidenceScore = backStackEntry.arguments?.getFloat("confidenceScore") ?: 0f

            ResultsScreen(
                imagePath = imagePath,
                thumbnailPath = thumbnailPath,
                isAIGenerated = isAIGenerated,
                confidenceScore = confidenceScore,
                onBackClick = { navController.popBackStack() },
                historyRepository = appContainer.historyRepository
            )
        }

        composable(
            route = "details/{detectionResultId}",
            arguments = listOf(
                navArgument("detectionResultId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val detectionResultId = backStackEntry.arguments?.getString("detectionResultId") ?: ""

            DetailsScreen(
                detectionResultId = detectionResultId,
                historyRepository = appContainer.historyRepository,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}