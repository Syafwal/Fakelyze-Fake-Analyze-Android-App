package com.wall.fakelyze.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed class yang merepresentasikan semua tujuan navigasi di aplikasi Fakelyze
 * dengan type-safety.
 */
sealed class FakelyzeDestination(val route: String) {

    /**
     * Mewakili route lengkap termasuk argumen
     */
    open fun createRoute(): String = route

    object Onboarding : FakelyzeDestination("onboarding")

    /** Home Screen - Halaman Utama */
    object Home : FakelyzeDestination("home")

    /** History Screen - Halaman History */
    object History : FakelyzeDestination("history")

    /** Info Screen - Halaman Informasi */
    object Info : FakelyzeDestination("info")

    /** Settings Screen - Halaman Pengaturan */
    object Settings : FakelyzeDestination("settings")

    /** Profile Screen - Halaman Profil Pengguna */
    object Profile : FakelyzeDestination("profile")

    /** Premium Screen - Halaman Upgrade Premium */
    object Premium : FakelyzeDestination("premium")

    /** Login Screen - Halaman Login */
    object Login : FakelyzeDestination("login")

    /** Register Screen - Halaman Registrasi */
    object Register : FakelyzeDestination("register")

    /** Results Screen - Halaman Hasil Deteksi */
    class Results(
        val resultId: String = ""
    ) : FakelyzeDestination("results") {

        override fun createRoute(): String {
            return "results/$resultId"
        }

        companion object {
            const val route = "results/{resultId}"

            val arguments = listOf(
                navArgument("resultId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        }
    }

    /** Details Screen - Halaman Detail Hasil */
    class Details(
        val detectionResultId: String = ""
    ) : FakelyzeDestination("details") {

        override fun createRoute(): String {
            return "details/$detectionResultId"
        }

        companion object {
            const val route = "details/{detectionResultId}"

            val arguments = listOf(
                navArgument("detectionResultId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        }
    }
}
