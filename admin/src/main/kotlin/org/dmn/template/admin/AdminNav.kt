package org.dmn.template.admin

import android.app.Activity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.dmn.template.DarkPu
import org.dmn.template.LightPu
import org.dmn.template.LocalPu
import org.dmn.template.PuAmber
import org.dmn.template.PuAmberInk

/** The admin app. Steps 1 to 5 (drivers, riders, pricing, disputes, finance) open from the home grid so far. */
@Composable
fun AdminNav() {
    val navController = rememberNavController()
    val dark = isSystemInDarkTheme()
    val pu = if (dark) DarkPu else LightPu

    // Status bar icons contrast with the background.
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(LocalPu provides pu) {
        MaterialTheme(
            colorScheme = if (dark) {
                darkColorScheme(
                    primary = PuAmber,
                    onPrimary = PuAmberInk,
                    background = pu.bg,
                    onBackground = pu.ink,
                    surface = pu.bg,
                    onSurface = pu.ink
                )
            } else {
                lightColorScheme(
                    primary = PuAmber,
                    onPrimary = PuAmberInk,
                    background = pu.bg,
                    onBackground = pu.ink,
                    surface = pu.bg,
                    onSurface = pu.ink
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pu.bg)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.weight(1f),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    composable("home") {
                        AdminHomeScreen(onOpenStep = { step ->
                            when (step) {
                                1 -> navController.navigate("drivers")
                                2 -> navController.navigate("riders")
                                3 -> navController.navigate("pricing")
                                4 -> navController.navigate("disputes")
                                5 -> navController.navigate("finance")
                            }
                        })
                    }
                    composable("drivers") {
                        AdminDriversScreen(
                            onBack = { navController.popBackStack() },
                            onOpen = { id -> navController.navigate("driver/$id") }
                        )
                    }
                    composable("finance") {
                        AdminFinanceScreen(
                            onBack = { navController.popBackStack() },
                            onOpenWallet = { id -> navController.navigate("wallet/$id") }
                        )
                    }
                    composable("wallet/{id}") { entry ->
                        AdminWalletScreen(
                            id = entry.arguments?.getString("id") ?: "",
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("disputes") {
                        AdminDisputesScreen(
                            onBack = { navController.popBackStack() },
                            onOpen = { id -> navController.navigate("dispute/$id") }
                        )
                    }
                    composable("dispute/{id}") { entry ->
                        AdminDisputeDetailScreen(
                            id = entry.arguments?.getString("id") ?: "",
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("pricing") {
                        AdminPricingScreen(onBack = { navController.popBackStack() })
                    }
                    composable("riders") {
                        AdminRidersScreen(
                            onBack = { navController.popBackStack() },
                            onOpen = { id -> navController.navigate("rider/$id") }
                        )
                    }
                    composable("rider/{id}") { entry ->
                        AdminRiderDetailScreen(
                            id = entry.arguments?.getString("id") ?: "",
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("driver/{id}") { entry ->
                        AdminDriverDetailScreen(
                            id = entry.arguments?.getString("id") ?: "",
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
