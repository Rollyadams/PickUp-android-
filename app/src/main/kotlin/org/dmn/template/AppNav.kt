package org.dmn.template

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun PickUpNav() {
    val navController = rememberNavController()

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
                    onOpenRequest = { id -> navController.navigate("request/$id") }
                )
            }
            composable(
                route = "request/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val request = id?.let { DriverRepository.request(it) }
                if (request != null) {
                    RequestDetailScreen(
                        request = request,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}