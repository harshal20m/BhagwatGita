package com.gitaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.data.model.AppTheme
import com.gitaapp.data.model.ReadingPreferences
import com.gitaapp.data.model.ThemeMode
import com.gitaapp.ui.navigation.GitaNavGraph
import com.gitaapp.ui.navigation.Screen
import com.gitaapp.ui.theme.GitaAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs    by preferencesManager.readingPreferences.collectAsStateWithLifecycle(ReadingPreferences())
            val appTheme by preferencesManager.appTheme.collectAsStateWithLifecycle(AppTheme.SAFFRON)
            val darkTheme = when (prefs.themeMode) {
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            GitaAppTheme(darkTheme = darkTheme, appTheme = appTheme) {
                GitaApp()
            }
        }
    }
}

// ── Scroll-to-hide nav state ──────────────────────────────────────────────────

class FloatingNavState {
    var isVisible by mutableStateOf(true)
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y < -8f) isVisible = false
            else if (available.y > 8f) isVisible = true
            return Offset.Zero
        }
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            isVisible = true; return super.onPostFling(consumed, available)
        }
    }
}

@Composable fun rememberFloatingNavState() = remember { FloatingNavState() }

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
private fun GitaApp() {
    val navController  = rememberNavController()
    val backEntry      by navController.currentBackStackEntryAsState()
    val currentRoute   = backEntry?.destination?.route
    val topLevel       = setOf(Screen.Home.route, Screen.Bookmarks.route, Screen.Search.route, Screen.Profile.route)
    val showNav        = currentRoute in topLevel
    val floatingNav    = rememberFloatingNavState()

    Box(Modifier.fillMaxSize()) {
        GitaNavGraph(
            navController = navController,
            modifier      = Modifier
                .fillMaxSize()
                .nestedScroll(floatingNav.nestedScrollConnection)
        )
        AnimatedVisibility(
            visible  = showNav && floatingNav.isVisible,
            enter    = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
            exit     = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomNav(
                currentRoute = currentRoute,
                onNavigate   = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )
        }
    }
}

// ── Floating pill nav bar ─────────────────────────────────────────────────────

private data class NavItem(val screen: Screen, val label: String, val sel: ImageVector, val unsel: ImageVector)
private val NAV_ITEMS = listOf(
    NavItem(Screen.Home,      "Home",    Icons.Filled.Home,     Icons.Outlined.Home),
    NavItem(Screen.Bookmarks, "Saved",   Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    NavItem(Screen.Search,    "Search",  Icons.Filled.Search,   Icons.Outlined.Search),
    NavItem(Screen.Profile,   "Profile", Icons.Filled.Person,   Icons.Outlined.Person)
)

@Composable
private fun FloatingBottomNav(currentRoute: String?, onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(0.18f))
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            NAV_ITEMS.forEach { item ->
                FloatingNavItem(item = item, isSelected = currentRoute == item.screen.route,
                    onClick = { onNavigate(item.screen) })
            }
        }
    }
}

@Composable
private fun FloatingNavItem(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgAlpha  by animateFloatAsState(if (isSelected) 1f else 0f, tween(200), label = "navBg")
    val iconSize by animateDpAsState(if (isSelected) 22.dp else 20.dp, tween(200), label = "iconSz")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = bgAlpha))
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }
            .padding(horizontal = if (isSelected) 16.dp else 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(if (isSelected) item.sel else item.unsel, item.label,
                tint     = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize))
            AnimatedVisibility(isSelected, enter = fadeIn(tween(200)), exit = fadeOut(tween(150))) {
                Text(item.label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}
