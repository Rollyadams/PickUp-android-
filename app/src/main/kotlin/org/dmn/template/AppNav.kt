package org.dmn.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private val idArgs = listOf(navArgument("id") { type = NavType.StringType })

private fun NavBackStackEntry.requestOrNull(): RideRequest? =
    arguments?.getString("id")?.let { DriverRepository.request(it) }

@Composable
fun PickUpNav() {
    val navController = rememberNavController()
    var online by rememberSaveable { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PuAmber,
            onPrimary = PuAmberInk,
            background = PuBg,
            onBackground = PuInk,
            surface = PuBg,
            onSurface = PuInk
        )
    ) {
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route

        Column(modifier = Modifier.fillMaxSize().background(PuBg)) {
        NavHost(
            navController = navController,
            startDestination = "phone",
            modifier = Modifier.weight(1f)
        ) {
            composable("phone") {
                PhoneEntryScreen(onSendCode = { navController.navigate("code") })
            }
            composable("code") {
                CodeEntryScreen(
                    onVerified = {
                        navController.navigate("home") {
                            popUpTo("phone") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("home") {
                DriverHomeScreen(
                    online = online,
                    onOnlineChange = { online = it },
                    onOpenRequest = { id -> navController.navigate("request/$id") }
                )
            }
            composable("income") { IncomeScreen() }
            composable("wallet") { WalletScreen() }
            composable("performance") { PerformanceScreen() }
            composable("request/{id}", arguments = idArgs) { entry ->
                entry.requestOrNull()?.let { request ->
                    RequestDetailScreen(
                        request = request,
                        onBack = { navController.popBackStack() },
                        onAccepted = {
                            online = true
                            navController.navigate("pickup/${request.id}") {
                                popUpTo("home")
                            }
                        }
                    )
                }
            }
            composable("pickup/{id}", arguments = idArgs) { entry ->
                entry.requestOrNull()?.let { request ->
                    PickupNavigationScreen(
                        request = request,
                        onStartTrip = {
                            navController.navigate("trip/${request.id}") {
                                popUpTo("home")
                            }
                        },
                        onCancel = { navController.popBackStack("home", inclusive = false) }
                    )
                }
            }
            composable("trip/{id}", arguments = idArgs) { entry ->
                entry.requestOrNull()?.let { request ->
                    TripInProgressScreen(
                        request = request,
                        onEndTrip = {
                            navController.navigate("complete/${request.id}") {
                                popUpTo("home")
                            }
                        }
                    )
                }
            }
            composable("complete/{id}", arguments = idArgs) { entry ->
                entry.requestOrNull()?.let { request ->
                    TripCompleteScreen(
                        request = request,
                        onDone = { navController.popBackStack("home", inclusive = false) }
                    )
                }
            }
        }

        if (currentRoute != null && currentRoute in tabRoutes) {
            DriverBottomBar(
                current = currentRoute,
                onSelect = { route ->
                    if (route != currentRoute) {
                        if (route == "home") {
                            navController.popBackStack("home", inclusive = false)
                        } else {
                            navController.navigate(route) {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
        }
    }
}
