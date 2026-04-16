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
import com.gitaapp.ui.screens.profile.ProfileScreen
import com.gitaapp.ui.screens.search.SearchScreen
import com.gitaapp.ui.screens.verse.VerseScreen

sealed class Screen(val route: String) {
    data object Home      : Screen("home")
    data object Bookmarks : Screen("bookmarks")
    data object Search    : Screen("search")
    data object Profile   : Screen("profile")

    data object Chapter : Screen("chapter/{chapterNumber}") {
        fun createRoute(n: Int) = "chapter/$n"
        const val ARG_CHAPTER_NUMBER = "chapterNumber"
    }
    data object Verse : Screen("verse/{chapterNumber}/{verseNumber}") {
        fun createRoute(c: Int, v: Int) = "verse/$c/$v"
        const val ARG_CHAPTER_NUMBER = "chapterNumber"
        const val ARG_VERSE_NUMBER   = "verseNumber"
    }
}

@Composable
fun GitaNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {

        composable(Screen.Home.route) {
            HomeScreen(
                onChapterClick    = { navController.navigate(Screen.Chapter.createRoute(it)) },
                onContinueReading = { c, v -> navController.navigate(Screen.Verse.createRoute(c, v)) },
                onVerseInChapterClick = { c, v -> navController.navigate(Screen.Verse.createRoute(c, v)) }
            )
        }
        composable(Screen.Bookmarks.route) {
            BookmarkScreen(onVerseClick = { c, v -> navController.navigate(Screen.Verse.createRoute(c, v)) })
        }
        composable(Screen.Search.route) {
            SearchScreen(onVerseClick = { c, v -> navController.navigate(Screen.Verse.createRoute(c, v)) })
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(
            Screen.Chapter.route,
            arguments = listOf(navArgument(Screen.Chapter.ARG_CHAPTER_NUMBER) { type = NavType.IntType })
        ) {
            ChapterScreen(
                onVerseClick  = { c, v -> navController.navigate(Screen.Verse.createRoute(c, v)) },
                onNavigateUp  = { navController.navigateUp() }
            )
        }
        composable(
            Screen.Verse.route,
            arguments = listOf(
                navArgument(Screen.Verse.ARG_CHAPTER_NUMBER) { type = NavType.IntType },
                navArgument(Screen.Verse.ARG_VERSE_NUMBER)   { type = NavType.IntType }
            )
        ) {
            VerseScreen(
                onNavigateUp       = { navController.navigateUp() },
                onNavigateToChapter = { c ->
                    navController.navigate(Screen.Chapter.createRoute(c)) {
                        popUpTo(Screen.Chapter.createRoute(c)) { inclusive = true }
                    }
                },
                onNavigateToVerse  = { c, v ->
                    navController.navigate(Screen.Verse.createRoute(c, v)) {
                        popUpTo(Screen.Verse.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
