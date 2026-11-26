package com.starter.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * The app's single nav graph. Every screen is reached through a presentation module's `XxxRoute`,
 * which takes plain lambdas for navigation — presentation modules never depend on navigation.
 *
 * There is no Splash destination: the system splash (SplashScreen API) covers the launch, and
 * [startDestination] is decided behind it by `LaunchViewModel`.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Screen,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Screen.Home> {
            PlaceholderHome()
        }

        // composable<Screen.Login> {
        //     LoginRoute(onLoggedIn = {
        //         navController.navigate(Screen.HomeGraph) { popUpTo<Screen.Login> { inclusive = true } }
        //     })
        // }
        // navigation<Screen.HomeGraph>(startDestination = Screen.HomeGraph.Orders) {
        //     composable<Screen.HomeGraph.Orders> { OrdersRoute(onOpenDetails = { id -> navController.navigate(Screen.OrderDetails(id)) }) }
        // }
    }
}

/** Replace with the first feature's `XxxRoute`. */
@Composable
private fun PlaceholderHome() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Starter kit — add your first feature", style = MaterialTheme.typography.titleMedium)
    }
}
