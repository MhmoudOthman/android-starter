package com.starter.app.navigation

import kotlinx.serialization.Serializable

/**
 * Typed routes for the app's single NavHost. Kept as `@Serializable` objects/classes so arguments
 * are type-checked and land in the destination's `SavedStateHandle` by property name — which is
 * where a ViewModel reads them from (carry an *id*, never a whole model, so the screen survives
 * process death by refetching).
 *
 * Group the destinations behind a drawer/bottom bar in a nested *graph* (not a nested NavHost):
 * one back stack, and one thing for the shell to test when deciding whether to show chrome.
 */
@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    // @Serializable data object Splash : Screen()
    // @Serializable data object Login : Screen()
    // @Serializable data object HomeGraph : Screen() {
    //     @Serializable data object Orders : Screen()
    //     @Serializable data object Settings : Screen()
    // }
    // @Serializable data class OrderDetails(val orderId: Int) : Screen()
}
