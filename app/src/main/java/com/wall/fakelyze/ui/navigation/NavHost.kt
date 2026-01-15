package com.wall.fakelyze.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wall.fakelyze.ui.screens.auth.LoginScreen
import com.wall.fakelyze.ui.screens.auth.RegisterScreen
import com.wall.fakelyze.ui.screens.details.DetailViewModel
import com.wall.fakelyze.ui.screens.details.DetailsScreen
import com.wall.fakelyze.ui.screens.history.HistoryScreen
import com.wall.fakelyze.ui.screens.history.HistoryViewModel
import com.wall.fakelyze.ui.screens.home.HomeScreen
import com.wall.fakelyze.ui.screens.home.HomeViewModel
import com.wall.fakelyze.ui.screens.onboarding.OnboardingScreens
import com.wall.fakelyze.ui.screens.premium.PremiumScreen
import com.wall.fakelyze.ui.screens.premium.PremiumViewModel
import com.wall.fakelyze.ui.screens.profile.ProfileScreen
import com.wall.fakelyze.ui.screens.profile.ProfileViewModel
import com.wall.fakelyze.ui.screens.results.ResultsScreen
import com.wall.fakelyze.ui.screens.results.ResultsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AIImageDetectorNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: FakelyzeDestination = FakelyzeDestination.Onboarding
) {
    // Get premium status from ProfileViewModel
    val profileViewModel: ProfileViewModel = koinViewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val isPremium = profileUiState.isPremium

    // PERBAIKAN CRITICAL: Buat HomeViewModel di level NavHost agar tidak hilang saat navigasi
    val sharedHomeViewModel: HomeViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(FakelyzeDestination.Onboarding.route) {
            OnboardingScreens(
                onFinishOnboarding = {
                    navController.navigate(FakelyzeDestination.Login.route) {
                        popUpTo(FakelyzeDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(FakelyzeDestination.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(FakelyzeDestination.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(FakelyzeDestination.Home.route) {
                        popUpTo(FakelyzeDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(FakelyzeDestination.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(FakelyzeDestination.Login.route)
                },
                onRegisterSuccess = {
                    navController.navigate(FakelyzeDestination.Home.route) {
                        popUpTo(FakelyzeDestination.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(FakelyzeDestination.Home.route) {
            android.util.Log.d("NavHost", "🔧 CREATING HOME SCREEN - Menggunakan shared ViewModel")

            HomeScreen(
                onNavigateToResults = { tempId, imagePath, isAI, confidence, explanation ->
                    android.util.Log.d("NavHost", "🎯 CALLBACK NAVIGASI TERPANGGIL!")
                    android.util.Log.d("NavHost", "📋 TempId: '$tempId'")
                    android.util.Log.d("NavHost", "📁 ImagePath: '$imagePath'")
                    android.util.Log.d("NavHost", "🤖 IsAI: $isAI")
                    android.util.Log.d("NavHost", "📊 Confidence: ${(confidence * 100).toInt()}%")
                    android.util.Log.d("NavHost", "📝 Explanation: ${explanation?.take(50) ?: "null"}")

                    try {
                        // PERBAIKAN: Validasi data sebelum navigasi
                        if (tempId.isBlank()) {
                            android.util.Log.e("NavHost", "❌ TempId kosong, tidak bisa navigasi")
                            return@HomeScreen
                        }

                        // PERBAIKAN: Validasi hasil tersimpan di temporary storage
                        val storedResult = sharedHomeViewModel.getTemporaryResult(tempId)
                        if (storedResult == null) {
                            android.util.Log.e("NavHost", "❌ Hasil tidak ditemukan di temporary storage")
                            return@HomeScreen
                        }

                        // PERBAIKAN: Gunakan FakelyzeDestination.Results dengan proper route creation
                        val destination = FakelyzeDestination.Results(tempId).createRoute()
                        android.util.Log.d("NavHost", "🧭 Navigasi ke: $destination")

                        // PERBAIKAN: Navigasi dengan validasi dan error handling
                        try {
                            navController.navigate(destination)
                            android.util.Log.d("NavHost", "✅ Navigasi berhasil ke ResultsScreen")
                        } catch (e: Exception) {
                            android.util.Log.e("NavHost", "❌ Error navigasi: ${e.message}")
                            // PERBAIKAN: Fallback jika navigasi gagal
                            try {
                                navController.navigate("results/$tempId")
                                android.util.Log.d("NavHost", "✅ Navigasi fallback berhasil")
                            } catch (fallbackError: Exception) {
                                android.util.Log.e("NavHost", "❌ Fallback navigation juga gagal: ${fallbackError.message}")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NavHost", "❌ Error dalam callback navigasi: ${e.message}")
                    }
                },
                onNavigateToPremium = {
                    android.util.Log.d("NavHost", "💎 Navigasi ke Premium")
                    navController.navigate(FakelyzeDestination.Premium.route)
                },
                viewModel = sharedHomeViewModel // GUNAKAN SHARED VIEWMODEL
            )

            android.util.Log.d("NavHost", "🔧 HOME SCREEN SETUP COMPLETE")
        }

        // History Screen
        composable(FakelyzeDestination.History.route) {
            HistoryScreen(
                viewModel = koinViewModel<HistoryViewModel>(),
                onItemClick = { detectionResultId ->
                    val destination = FakelyzeDestination.Details(detectionResultId).createRoute()
                    navController.navigate(destination)
                },
                isPremium = isPremium, // Pass premium status
                onUpgradeToPremium = {
                    navController.navigate(FakelyzeDestination.Premium.route)
                }
            )
        }

        // Profile Screen
        composable(FakelyzeDestination.Profile.route) {
            ProfileScreen(
                viewModel = koinViewModel<ProfileViewModel>(),
                navController = navController
            )
        }

        // Results Screen dengan proper error handling dan retry mechanism
        composable(
            route = FakelyzeDestination.Results.route,
            arguments = FakelyzeDestination.Results.arguments
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getString("resultId") ?: ""
            android.util.Log.d("NavHost", "📋 === RESULTS SCREEN COMPOSABLE DIMULAI ===")
            android.util.Log.d("NavHost", "📋 ResultId dari arguments: '$resultId'")

            // PERBAIKAN CRITICAL: Gunakan SHARED HomeViewModel yang sama!
            val resultsViewModel: ResultsViewModel = koinViewModel()

            // PERBAIKAN: Validasi parameter yang diperlukan dengan immediate fallback
            if (resultId.isBlank()) {
                android.util.Log.e("NavHost", "❌ ❌ ❌ CRITICAL: ResultId kosong!")

                // FALLBACK UI untuk resultId kosong
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌ Error: ID hasil tidak ditemukan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Kembali")
                        }
                    }
                }
                return@composable
            }

            // PERBAIKAN: LANGSUNG ambil data dari SHARED HomeViewModel
            android.util.Log.d("NavHost", "🔍 Mengambil data dari SHARED temporary storage dengan resultId: '$resultId'")
            val detectionResult = sharedHomeViewModel.getTemporaryResult(resultId)

            if (detectionResult != null) {
                android.util.Log.d("NavHost", "✅ ✅ ✅ DATA DITEMUKAN di SHARED ViewModel!")
                android.util.Log.d("NavHost", "📊 Data Details:")
                android.util.Log.d("NavHost", "   - ID: ${detectionResult.id}")
                android.util.Log.d("NavHost", "   - ImagePath: '${detectionResult.imagePath}'")
                android.util.Log.d("NavHost", "   - IsAI: ${detectionResult.isAIGenerated}")
                android.util.Log.d("NavHost", "   - Confidence: ${(detectionResult.confidenceScore * 100).toInt()}%")
                android.util.Log.d("NavHost", "   - Explanation: ${detectionResult.explanation?.take(50) ?: "null"}")

                // LANGSUNG tampilkan ResultsScreen dengan data
                ResultsScreen(
                    viewModel = resultsViewModel,
                    onBackClick = {
                        android.util.Log.d("NavHost", "⬅️ Tombol kembali ditekan dari ResultsScreen")
                        sharedHomeViewModel.clearTemporaryResult(resultId)
                        navController.popBackStack()
                    },
                    isPremium = isPremium,
                    onUpgradeToPremium = {
                        android.util.Log.d("NavHost", "💎 Navigasi ke Premium dari Results")
                        navController.navigate(FakelyzeDestination.Premium.route)
                    },
                    // TERUSKAN semua data sebagai parameter
                    imagePath = detectionResult.imagePath,
                    thumbnailPath = detectionResult.thumbnailPath,
                    isAIGenerated = detectionResult.isAIGenerated,
                    confidenceScore = detectionResult.confidenceScore,
                    explanation = detectionResult.explanation
                )
            } else {
                android.util.Log.e("NavHost", "❌ ❌ ❌ CRITICAL: DATA TIDAK DITEMUKAN di SHARED ViewModel!")
                android.util.Log.e("NavHost", "📊 Available tempIds in SHARED: ${sharedHomeViewModel.getAvailableTemporaryResultIds()}")

                // FALLBACK UI untuk data tidak ditemukan
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌ Data hasil deteksi tidak ditemukan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ID: $resultId",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Silakan scan ulang gambar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Kembali ke Home")
                        }
                    }
                }
            }
        }

        // Details Screen
        composable(
            route = FakelyzeDestination.Details.route,
            arguments = FakelyzeDestination.Details.arguments
        ) { backStackEntry ->
            val detectionResultId = backStackEntry.arguments?.getString("detectionResultId") ?: ""

            DetailsScreen(
                detectionResultId = detectionResultId,
                viewModel = koinViewModel<DetailViewModel>(),
                onBackClick = { navController.popBackStack() },
                isPremium = isPremium, // Pass premium status
                onUpgradeToPremium = {
                    navController.navigate(FakelyzeDestination.Premium.route)
                }
            )
        }

        // Premium Screen
        composable(FakelyzeDestination.Premium.route) {
            PremiumScreen(
                viewModel = koinViewModel<PremiumViewModel>(),
                navController = navController
            )
        }
    }
}
