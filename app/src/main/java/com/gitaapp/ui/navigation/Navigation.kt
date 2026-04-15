package com.gitaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gitaapp.ui.screens.bookmark.BookmarkScreen
import com.gitaapp.ui.screens.chapter.ChapterScreen
import com.gitaapp.ui.screens.home.HomeScreen
import com.gitaapp.ui.screens.search.SearchScreen
import com.gitaapp.ui.screens.settings.SettingsScreen
import com.gitaapp.ui.screens.verse.VerseScreen

/**
 * Sealed class representing all navigable destinations in the app.
 * Using typed route objects prevents magic-string bugs at the call site.
 */
sealed class Screen(val route: String) {

    // ── Bottom nav destinations ───────────────────────────────────────────
    data object Home : Screen("home")
    data object Bookmarks : Screen("bookmarks")
    data object Search : Screen("search")
    data object Settings : Screen("settings")

    // ── Deep destinations ─────────────────────────────────────────────────
    data object Chapter : Screen("chapter/{chapterNumber}") {
        fun createRoute(chapterNumber: Int) = "chapter/$chapterNumber"
        const val ARG_CHAPTER_NUMBER = "chapterNumber"
    }

    data object Verse : Screen("verse/{chapterNumber}/{verseNumber}") {
        fun createRoute(chapterNumber: Int, verseNumber: Int) =
            "verse/$chapterNumber/$verseNumber"
        const val ARG_CHAPTER_NUMBER = "chapterNumber"
        const val ARG_VERSE_NUMBER = "verseNumber"
    }
}

/** Routes used in bottom navigation — ordered as they appear in the nav bar. */
val bottomNavScreens = listOf(Screen.Home, Screen.Bookmarks, Screen.Search)

/**
 * Root NavHost for the app.
 * All screen-level composables are registered here.
 */
@Composable
fun GitaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        // ── Bottom nav ────────────────────────────────────────────────────
        composable(route = Screen.Home.route) {
            HomeScreen(
                onChapterClick = { chapterNumber ->
                    navController.navigate(Screen.Chapter.createRoute(chapterNumber))
                },
                onContinueReading = { chapterNumber, verseNumber ->
                    navController.navigate(Screen.Verse.createRoute(chapterNumber, verseNumber))
                }
            )
        }

        composable(route = Screen.Bookmarks.route) {
            BookmarkScreen(
                onVerseClick = { chapterNumber, verseNumber ->
                    navController.navigate(Screen.Verse.createRoute(chapterNumber, verseNumber))
                }
            )
        }

        composable(route = Screen.Search.route) {
            SearchScreen(
                onVerseClick = { chapterNumber, verseNumber ->
                    navController.navigate(Screen.Verse.createRoute(chapterNumber, verseNumber))
                }
            )
        }

        // ── Settings screen ────────────────────────────────────────────────
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // ── Chapter screen ────────────────────────────────────────────────
        composable(
            route = Screen.Chapter.route,
            arguments = listOf(
                navArgument(Screen.Chapter.ARG_CHAPTER_NUMBER) {
                    type = NavType.IntType
                }
            )
        ) {
            ChapterScreen(
                onVerseClick = { chapterNumber, verseNumber ->
                    navController.navigate(Screen.Verse.createRoute(chapterNumber, verseNumber))
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // ── Verse screen ──────────────────────────────────────────────────
        composable(
            route = Screen.Verse.route,
            arguments = listOf(
                navArgument(Screen.Verse.ARG_CHAPTER_NUMBER) { type = NavType.IntType },
                navArgument(Screen.Verse.ARG_VERSE_NUMBER) { type = NavType.IntType }
            )
        ) {
            VerseScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToChapter = { chapterNumber ->
                    navController.navigate(Screen.Chapter.createRoute(chapterNumber)) {
                        popUpTo(Screen.Chapter.createRoute(chapterNumber)) { inclusive = true }
                    }
                },
                onNavigateToVerse = { chapterNumber, verseNumber ->
                    // Replace the current verse on the back stack so prev/next
                    // doesn't build up an ever-growing stack of verse destinations.
                    navController.navigate(Screen.Verse.createRoute(chapterNumber, verseNumber)) {
                        popUpTo(Screen.Verse.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
