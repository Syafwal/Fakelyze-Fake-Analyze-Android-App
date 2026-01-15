package com.wall.fakelyze.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    data object Home : Screen()
}