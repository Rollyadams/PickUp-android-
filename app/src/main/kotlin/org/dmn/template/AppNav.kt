package org.dmn.template

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

private val idArgs = listOf(navArgument("id") { type = NavType.StringType })

private fun NavBackStackEntry.requestOrNull(): RideRequest? =
    arguments?.getString("id")?.let { DriverRepository.request(it) }

@Composable
fun PickUpNav() {
    val navController = rememberNavController()
    var online by rememberSaveable { mutableStateOf(false) }

    // Appearance: 0 = Auto (follow the phone), 1 = Light, 2 = Dark. Remembered between launches.
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pickup_prefs", Context.MODE_PRIVATE) }
    var themeMode by remember { mutableIntStateOf(prefs.getInt("theme_mode", 0)) }
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    val pu = if (dark) DarkPu else LightPu

    // Status bar and navigation bar icons must contrast with the app background.
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var menuMessage by remember { mutableStateOf<String?>(null) }

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
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val onTab = currentRoute != null && currentRoute in tabRoutes
            val openMenu: () -> Unit = { scope.launch { drawerState.open() } }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = onTab,
                drawerContent = {
                    DriverDrawer(
                        themeMode = themeMode,
                        onThemeMode = { mode ->
                            themeMode = mode
                            prefs.edit().putInt("theme_mode", mode).apply()
                        },
                        onItem = { label ->
                            scope.launch { drawerState.close() }
                            if (label == "Profile & documents") {
                                navController.navigate("profile") { launchSingleTop = true }
                            } else {
                                menuMessage = "$label comes in a later batch."
                            }
                        },
                        onLogout = {
                            scope.launch { drawerState.close() }
                            online = false
                            navController.navigate("phone") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize().background(pu.bg)) {
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
                                    navController.navigate("onboarding") {
                                        popUpTo("phone") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("onboarding") {
                            OnboardingScreen(
                                onSubmitted = {
                                    navController.navigate("pending") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("pending") {
                            VerificationPendingScreen(
                                onDemoContinue = {
                                    navController.navigate("home") {
                                        popUpTo("pending") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }
                        composable("home") {
                            DriverHomeScreen(
                                online = online,
                                onOnlineChange = { online = it },
                                onOpenRequest = { id -> navController.navigate("request/$id") },
                                onMenu = openMenu
                            )
                        }
                        composable("income") { IncomeScreen(onMenu = openMenu) }
                        composable("wallet") { WalletScreen(onMenu = openMenu) }
                        composable("performance") { PerformanceScreen(onMenu = openMenu) }
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

                    if (onTab) {
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

            menuMessage?.let { message ->
                SkeletonDialog(message = message, onOk = { menuMessage = null })
            }
        }
    }
}
