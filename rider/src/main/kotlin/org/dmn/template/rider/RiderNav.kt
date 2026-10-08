package org.dmn.template.rider

import android.app.Activity
import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.dmn.template.DarkPu
import org.dmn.template.LightPu
import org.dmn.template.LocalPu
import org.dmn.template.PrimaryButton
import org.dmn.template.PuAmber
import org.dmn.template.PuAmberInk
import org.dmn.template.PuBg
import org.dmn.template.PuDanger
import org.dmn.template.PuInk
import org.dmn.template.PuLine
import org.dmn.template.PuMuted
import org.dmn.template.RiderAvatar

private class MenuEntry(val route: String, val glyph: String, val label: String)

// The rider menu is its own list, different from the driver's (no wallet, no registration).
private val riderMenu = listOf(
    MenuEntry("history", "🕘", "Trip history"),
    MenuEntry("notifications", "🔔", "Notifications"),
    MenuEntry("safety", "🛡️", "Safety"),
    MenuEntry("settings", "⚙️", "Settings"),
    MenuEntry("help", "❓", "Help"),
    MenuEntry("support", "💬", "Support")
)

/**
 * The rider side of the single Pick Up app.
 * [onSwitchToDriver] is called from the "Driver app" button; [onLoggedOut] lets the app root
 * sign the driver side out too.
 */
@Composable
fun RiderNav(onSwitchToDriver: () -> Unit, onLoggedOut: () -> Unit) {
    val navController = rememberNavController()
    val draft = remember { RideDraft() }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(RIDER_PREFS, Context.MODE_PRIVATE) }
    var phone by remember { mutableStateOf(prefs.getString("phone", "") ?: "") }
    val startDestination = remember {
        if (prefs.getBoolean(RIDER_LOGGED_IN, false)) "home" else "phone"
    }

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
            val onHome = backStack?.destination?.route == "home"
            val openMenu: () -> Unit = { scope.launch { drawerState.open() } }
            val logout: () -> Unit = {
                scope.launch { drawerState.close() }
                prefs.edit().putBoolean(RIDER_LOGGED_IN, false).apply()
                onLoggedOut()
                draft.reset()
                navController.navigate("phone") {
                    popUpTo("home") { inclusive = true }
                }
            }
            // Back to the first screen, forgetting the ride in progress.
            val backToHome: () -> Unit = {
                draft.reset()
                navController.popBackStack("home", false)
            }
            val openSearch: (Int, Int) -> Unit = { target, stopIndex ->
                draft.searchTarget = target
                draft.searchStopIndex = stopIndex
                navController.navigate("search")
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = onHome,
                drawerContent = {
                    RiderDrawer(
                        unread = RIDER_UNREAD,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navController.navigate(route) { launchSingleTop = true }
                        },
                        onSwitchToDriver = {
                            scope.launch { drawerState.close() }
                            onSwitchToDriver()
                        }
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize().background(pu.bg)) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.weight(1f),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        composable("phone") {
                            RiderPhoneScreen(onSendCode = { number ->
                                phone = number
                                prefs.edit().putString("phone", number).apply()
                                navController.navigate("code")
                            })
                        }
                        composable("code") {
                            RiderCodeScreen(
                                onVerified = {
                                    prefs.edit().putBoolean(RIDER_LOGGED_IN, true).apply()
                                    navController.navigate("home") {
                                        popUpTo("phone") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("home") {
                            RiderHomeScreen(
                                draft = draft,
                                onMenu = openMenu,
                                onEditPickup = { openSearch(0, draft.stops.size) },
                                onWhereTo = { openSearch(1, draft.stops.size) }
                            )
                        }
                        composable("search") {
                            RiderSearchScreen(
                                draft = draft,
                                onClose = { navController.popBackStack() },
                                onPicked = {
                                    if (!navController.popBackStack("fare", false)) {
                                        navController.navigate("fare") {
                                            popUpTo("search") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("fare") {
                            RiderFareScreen(
                                draft = draft,
                                onBack = {
                                    draft.stops.clear()
                                    navController.popBackStack("home", false)
                                },
                                onEditPickup = { openSearch(0, draft.stops.size) },
                                onEditStop = { index -> openSearch(1, index) },
                                onAddStop = { openSearch(1, draft.stops.size) },
                                onFind = { navController.navigate("finding") },
                                onQuickAccept = {
                                    draft.driver = RiderDemo.offersFor(draft.fare).first()
                                        .copy(fare = draft.fare, etaMinutes = 3)
                                    navController.navigate("matched")
                                }
                            )
                        }
                        composable("finding") {
                            RiderFindingScreen(
                                draft = draft,
                                onAccept = { offer ->
                                    draft.driver = offer
                                    draft.fare = offer.fare
                                    navController.navigate("matched") {
                                        popUpTo("finding") { inclusive = true }
                                    }
                                },
                                onCancel = backToHome
                            )
                        }
                        composable("matched") {
                            RiderMatchedScreen(
                                draft = draft,
                                onStartTrip = {
                                    navController.navigate("trip") {
                                        popUpTo("matched") { inclusive = true }
                                    }
                                },
                                onCancel = backToHome
                            )
                        }
                        composable("trip") {
                            RiderTripScreen(
                                draft = draft,
                                onArrived = {
                                    navController.navigate("complete") {
                                        popUpTo("trip") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("complete") {
                            RiderCompleteScreen(
                                draft = draft,
                                onDone = {
                                    navController.navigate("rate") {
                                        popUpTo("complete") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("rate") {
                            RiderRateScreen(
                                draft = draft,
                                onFinish = {
                                    draft.reset()
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("history") {
                            RiderHistoryScreen(onBack = { navController.popBackStack() })
                        }
                        composable("profile") {
                            RiderProfileScreen(phone = phone, onBack = { navController.popBackStack() })
                        }
                        composable("settings") {
                            RiderSettingsScreen(
                                draft = draft,
                                onBack = { navController.popBackStack() },
                                onLogout = logout
                            )
                        }
                        composable("notifications") {
                            RiderInfoScreen("Notifications", notificationEntries) {
                                navController.popBackStack()
                            }
                        }
                        composable("safety") {
                            RiderInfoScreen("Safety", safetyEntries) { navController.popBackStack() }
                        }
                        composable("help") {
                            RiderInfoScreen("Help", helpEntries) { navController.popBackStack() }
                        }
                        composable("support") {
                            RiderInfoScreen("Support", supportEntries) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiderDrawer(
    unread: Int,
    onNavigate: (String) -> Unit,
    onSwitchToDriver: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = PuBg, drawerContentColor = PuInk) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Tapping the header opens the rider's profile.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("profile") }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiderAvatar("Rider", 56.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rider", color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("★ 5.00", color = PuMuted, fontSize = 13.sp)
                }
                Text("›", color = PuMuted, fontSize = 24.sp)
            }
            HairLine()

            riderMenu.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(entry.route) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(entry.glyph, fontSize = 20.sp)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        entry.label,
                        color = PuInk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (entry.route == "notifications" && unread > 0) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PuDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$unread",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            HairLine()
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                PrimaryButton("Driver app", onSwitchToDriver)
            }
        }
    }
}
