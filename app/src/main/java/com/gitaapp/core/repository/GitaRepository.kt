package com.gitaapp.core.repository

import com.gitaapp.data.model.Bookmark
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.ReadingProgress
import com.gitaapp.data.model.SearchResult
import com.gitaapp.data.model.Verse
import kotlinx.coroutines.flow.Flow

/**
 * Contract for all Gita data operations.
 * The UI layer depends on this interface, not the implementation,
 * enabling easy testing via fakes.
 */
interface GitaRepository {

    // ── Chapters ──────────────────────────────────────────────────────────

    /** Observe all chapters with their reading progress. Emits on every DB change. */
    fun observeAllChapters(): Flow<List<Chapter>>

    /** Observe a single chapter by number. */
    fun observeChapter(chapterNumber: Int): Flow<Chapter?>

    // ── Verses ────────────────────────────────────────────────────────────

    /** Observe all verses for a given chapter. */
    fun observeVersesForChapter(chapterNumber: Int): Flow<List<Verse>>

    /** Observe a single verse by its composite ID (e.g. "1.1"). */
    fun observeVerse(verseId: String): Flow<Verse?>

    /** Observe total verse count. */
    fun observeVerseCount(): Flow<Int>

    /** Get a random verse — for "Verse of the Day" feature. */
    suspend fun getRandomVerse(): Verse?

    // ── Bookmarks ─────────────────────────────────────────────────────────

    /** Observe all bookmarks, newest first. */
    fun observeAllBookmarks(): Flow<List<Bookmark>>

    /** Observe total bookmark count. */
    fun observeBookmarkCount(): Flow<Int>

    /** Add a bookmark for a verse. */
    suspend fun addBookmark(verseId: String)

    /** Remove a bookmark for a verse. */
    suspend fun removeBookmark(verseId: String)

    /** Toggle bookmark — adds if not present, removes if present. */
    suspend fun toggleBookmark(verseId: String)

    // ── Reading Progress ──────────────────────────────────────────────────

    /** Update reading progress when user views a verse. */
    suspend fun updateReadingProgress(chapterNumber: Int, verseNumber: Int)

    /** Observe the last chapter the user was reading. */
    fun observeLastReadChapter(): Flow<ReadingProgress?>

    /** Observe overall reading progress (0.0–1.0). */
    fun observeOverallProgress(): Flow<Float>

    // ── Search ────────────────────────────────────────────────────────────

    /** Search across translations, transliterations, and commentaries. */
    fun searchVerses(query: String): Flow<List<SearchResult>>
}