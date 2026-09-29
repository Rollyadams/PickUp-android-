package org.dmn.template

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        colorScheme = darkColorScheme(
            primary = PuAmber,
            onPrimary = PuAmberInk,
            background = PuNavy,
            onBackground = Color.White,
            surface = PuNavy,
            onSurface = Color.White
        )
    ) {
        NavHost(navController = navController, startDestination = "phone") {
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
    }
}
