package com.passer.passwatch.screen

sealed class Screen(val route: String) {
    data object MainMenuScreen : Screen("main_menu_screen")
    data object NewRideScreen : Screen("new_ride_screen")
    data object RidesScreen : Screen("rides_screen")
    data object MapScreen : Screen("map_screen")
    data object SettingsScreen : Screen("settings_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}