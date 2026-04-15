package com.gitaapp.data.model

import androidx.compose.runtime.Immutable

/**
 * Domain model representing a single chapter of the Bhagavad Gita.
 * All properties are immutable for Compose stability.
 */
@Immutable
data class Chapter(
    val number: Int,
    val nameTransliterated: String,
    val nameSanskrit: String,
    val nameMeaning: String,
    val summary: String,
    val verseCount: Int,
    val readingProgressPercent: Float = 0f,
    val isStarted: Boolean = false
)

/**
 * Domain model representing a single verse (shloka) of the Bhagavad Gita.
 */
@Immutable
data class Verse(
    val id: String,           // e.g. "1.1"
    val chapterNumber: Int,
    val verseNumber: Int,
    val sanskritText: String,
    val transliteration: String,
    val wordMeanings: String,
    val translation: String,
    val commentary: String,
    val isBookmarked: Boolean = false
)

/**
 * Domain model for a bookmarked verse with chapter context.
 */
@Immutable
data class Bookmark(
    val verseId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val chapterName: String,
    val sanskritText: String,
    val translation: String,
    val bookmarkedAt: Long
)

/**
 * Represents a user's reading progress for a chapter.
 */
@Immutable
data class ReadingProgress(
    val chapterNumber: Int,
    val lastReadVerseNumber: Int,
    val totalVerses: Int,
    val lastReadAt: Long
) {
    val progressPercent: Float
        get() = if (totalVerses == 0) 0f
                else (lastReadVerseNumber.toFloat() / totalVerses.toFloat()).coerceIn(0f, 1f)
}

/**
 * Search result domain model.
 */
@Immutable
data class SearchResult(
    val verseId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val translation: String,
    val chapterName: String,
    val matchHighlight: String
)

/**
 * User preferences for reading experience.
 */
@Immutable
data class ReadingPreferences(
    val fontSize: FontSize = FontSize.MEDIUM,
    val showTransliteration: Boolean = true,
    val showWordMeanings: Boolean = true,
    val showCommentary: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

enum class FontSize(val scale: Float, val label: String) {
    SMALL(0.85f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.2f, "Large"),
    EXTRA_LARGE(1.4f, "Extra Large")
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
