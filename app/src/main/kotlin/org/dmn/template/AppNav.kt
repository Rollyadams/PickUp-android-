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

    // Settings, remembered between launches.
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pickup_prefs", Context.MODE_PRIVATE) }
    // Appearance: 0 = Auto (follow the phone), 1 = Light, 2 = Dark.
    var themeMode by remember { mutableIntStateOf(prefs.getInt("theme_mode", 0)) }
    // Navigation app: 0 = Google Maps, 1 = Waze.
    var navApp by remember { mutableIntStateOf(prefs.getInt("nav_app", 0)) }
    var soundsOn by remember { mutableStateOf(prefs.getBoolean("sounds_on", true)) }
    var keepScreenOn by remember { mutableStateOf(prefs.getBoolean("keep_screen_on", false)) }

    // Where the driver got to: 0 = logged out, 1 = signing up, 2 = waiting for approval, 3 = approved.
    // Launching the app resumes from there, so nobody logs in again unless they logged out.
    fun saveStage(stage: Int) = prefs.edit().putInt("stage", stage).apply()
    val startDestination = remember {
        when (prefs.getInt("stage", 0)) {
            3 -> "home"
            2 -> "pending"
            1 -> "onboarding"
            else -> "phone"
        }
    }

    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    val pu = if (dark) DarkPu else LightPu

    // Status bar icons contrast with the background; the screen stays on if the driver chose that.
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
        view.keepScreenOn = keepScreenOn
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            val logout: () -> Unit = {
                scope.launch { drawerState.close() }
                online = false
                saveStage(0)
                navController.navigate("phone") {
                    popUpTo("home") { inclusive = true }
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = onTab,
                drawerContent = {
                    DriverDrawer(
                        unread = DriverRepository.unreadCount(),
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navController.navigate(route) { launchSingleTop = true }
                        },
                        onLogout = logout
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize().background(pu.bg)) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable("phone") {
                            PhoneEntryScreen(onSendCode = { navController.navigate("code") })
                        }
                        composable("code") {
                            CodeEntryScreen(
                                onVerified = {
                                    saveStage(1)
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
                                    saveStage(2)
                                    navController.navigate("pending") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("pending") {
                            VerificationPendingScreen(
                                onDemoContinue = {
                                    saveStage(3)
                                    navController.navigate("home") {
                                        popUpTo("pending") { inclusive = true }
                                    }
                                }
                            )
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
                        composable("performance") {
                            PerformanceScreen(
                                onMenu = openMenu,
                                onReviews = { navController.navigate("ratings") }
                            )
                        }

                        // ----- Menu screens -----
                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }
                        composable("notifications") {
                            NotificationsScreen(onBack = { navController.popBackStack() })
                        }
                        composable("safety") {
                            SafetyScreen(
                                onBack = { navController.popBackStack() },
                                onSupport = { navController.navigate("support") { launchSingleTop = true } }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                themeMode = themeMode,
                                onThemeMode = { mode ->
                                    themeMode = mode
                                    prefs.edit().putInt("theme_mode", mode).apply()
                                },
                                navApp = navApp,
                                onNavApp = { app ->
                                    navApp = app
                                    prefs.edit().putInt("nav_app", app).apply()
                                },
                                soundsOn = soundsOn,
                                onSoundsOn = { value ->
                                    soundsOn = value
                                    prefs.edit().putBoolean("sounds_on", value).apply()
                                },
                                keepScreenOn = keepScreenOn,
                                onKeepScreenOn = { value ->
                                    keepScreenOn = value
                                    prefs.edit().putBoolean("keep_screen_on", value).apply()
                                },
                                onBack = { navController.popBackStack() },
                                onLogout = logout
                            )
                        }
                        composable("help") { HelpScreen(onBack = { navController.popBackStack() }) }
                        composable("support") { SupportScreen(onBack = { navController.popBackStack() }) }
                        composable("invite") { InviteScreen(onBack = { navController.popBackStack() }) }
                        composable("ratings") { RatingsScreen(onBack = { navController.popBackStack() }) }

                        // ----- Ride flow -----
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
        }
    }
}
