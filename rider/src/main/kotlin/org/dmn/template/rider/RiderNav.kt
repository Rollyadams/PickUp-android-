package org.dmn.template.rider

import android.app.Activity
import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.dmn.template.PuAmber
import org.dmn.template.PuAmberInk
import org.dmn.template.PuBg
import org.dmn.template.PuDanger
import org.dmn.template.PuInk
import org.dmn.template.PuMuted

private const val DRAWER_HISTORY = "history"
private const val DRAWER_PROFILE = "profile"

@Composable
fun RiderNav() {
    val navController = rememberNavController()
    val draft = remember { RideDraft() }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("rider_prefs", Context.MODE_PRIVATE) }
    var phone by remember { mutableStateOf(prefs.getString("phone", "") ?: "") }
    val startDestination = remember { if (prefs.getBoolean("logged_in", false)) "home" else "phone" }

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
                prefs.edit().putBoolean("logged_in", false).apply()
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

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = onHome,
                drawerContent = {
                    RiderDrawer(
                        phone = phone,
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
                                    prefs.edit().putBoolean("logged_in", true).apply()
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
                                onContinue = { navController.navigate("fare") }
                            )
                        }
                        composable("fare") {
                            RiderFareScreen(
                                draft = draft,
                                onBack = { navController.popBackStack() },
                                onFind = { navController.navigate("finding") },
                                onQuickAccept = {
                                    draft.driver = RiderDemo.offersFor(draft.fare).first()
                                        .copy(fare = draft.fare)
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
                        composable(DRAWER_HISTORY) {
                            RiderHistoryScreen(onBack = { navController.popBackStack() })
                        }
                        composable(DRAWER_PROFILE) {
                            RiderProfileScreen(
                                draft = draft,
                                phone = phone,
                                onBack = { navController.popBackStack() },
                                onLogout = logout
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiderDrawer(phone: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    ModalDrawerSheet(drawerContainerColor = PuBg, drawerContentColor = PuInk) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Pick Up", color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(phone.ifEmpty { "Rider" }, color = PuMuted, fontSize = 14.sp)
            }
            HairLine()
            DrawerItem("🕘", "Trip history") { onNavigate(DRAWER_HISTORY) }
            DrawerItem("👤", "Profile and settings") { onNavigate(DRAWER_PROFILE) }
            Spacer(Modifier.weight(1f))
            HairLine()
            Text(
                "Sign out",
                color = PuDanger,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun DrawerItem(glyph: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(glyph, fontSize = 20.sp)
        Spacer(Modifier.width(14.dp))
        Text(label, color = PuInk, fontSize = 16.sp)
    }
}
