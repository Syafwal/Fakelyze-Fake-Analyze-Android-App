package com.wall.fakelyze.di

import com.wall.fakelyze.ui.screens.details.DetailViewModel
import com.wall.fakelyze.ui.screens.history.HistoryViewModel
import com.wall.fakelyze.ui.screens.home.HomeViewModel
import com.wall.fakelyze.ui.screens.onboarding.OnboardingViewModel
import com.wall.fakelyze.ui.screens.premium.PremiumViewModel
import com.wall.fakelyze.ui.screens.profile.ProfileViewModel
import com.wall.fakelyze.ui.screens.results.ResultsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels with error handling
 */
val viewModelModule = module {
    // ViewModels dengan safe dependency injection
    viewModel {
        try {
            DetailViewModel(get())
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Error creating DetailViewModel", e)
            throw e
        }
    }

    viewModel {
        try {
            HistoryViewModel(get())
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Error creating HistoryViewModel", e)
            throw e
        }
    }

    viewModel {
        try {
            ResultsViewModel(get())
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Error creating ResultsViewModel", e)
            throw e
        }
    }

    viewModel {
        try {
            OnboardingViewModel()
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Error creating OnboardingViewModel", e)
            throw e
        }
    }

    viewModel {
        try {
            ProfileViewModel(get(), get(), get())
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Error creating ProfileViewModel", e)
            throw e
        }
    }

    viewModel {
        try {
            PremiumViewModel(get(), get())
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Error creating PremiumViewModel", e)
            throw e
        }
    }

    // HomeViewModel dengan error handling
    viewModel<HomeViewModel> {
        try {
            HomeViewModel(
                application = get(),
                imageClassifier = get(),
                userPreferencesRepository = get(),
                premiumRepository = get(),
                historyRepository = get(),
                savedStateHandle = get()
            )
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Failed to create HomeViewModel", e)
            throw IllegalStateException("Critical: HomeViewModel failed to initialize", e)
        }
    }

    // HistoryViewModel dengan error handling
    viewModel<HistoryViewModel> {
        try {
            HistoryViewModel(
                historyRepository = get()
            )
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Failed to create HistoryViewModel", e)
            throw IllegalStateException("Critical: HistoryViewModel failed to initialize", e)
        }
    }

    // PremiumViewModel dengan error handling
    viewModel<PremiumViewModel> {
        try {
            PremiumViewModel(
                premiumRepository = get(),
                userPreferencesRepository = get()
            )
        } catch (e: Exception) {
            android.util.Log.e("ViewModelModule", "Failed to create PremiumViewModel", e)
            throw IllegalStateException("Critical: PremiumViewModel failed to initialize", e)
        }
    }
}
