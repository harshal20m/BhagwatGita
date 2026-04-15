package com.gitaapp.core.repository.impl

import com.gitaapp.core.database.dao.BookmarkDao
import com.gitaapp.core.database.dao.BookmarkRow
import com.gitaapp.core.database.dao.ChapterDao
import com.gitaapp.core.database.dao.ChapterWithProgress
import com.gitaapp.core.database.dao.ProgressWithTotal
import com.gitaapp.core.database.dao.ReadingProgressDao
import com.gitaapp.core.database.dao.VerseDao
import com.gitaapp.core.database.dao.VerseWithBookmark
import com.gitaapp.core.database.entity.BookmarkEntity
import com.gitaapp.core.database.entity.ReadingProgressEntity
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.Bookmark
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.ReadingProgress
import com.gitaapp.data.model.SearchResult
import com.gitaapp.data.model.Verse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitaRepositoryImpl @Inject constructor(
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val bookmarkDao: BookmarkDao,
    private val readingProgressDao: ReadingProgressDao
) : GitaRepository {

    // ── Chapters ──────────────────────────────────────────────────────────

    override fun observeAllChapters(): Flow<List<Chapter>> =
        chapterDao.observeAllChapters().map { list ->
            list.map { it.toDomain() }
        }

    override fun observeChapter(chapterNumber: Int): Flow<Chapter?> =
        chapterDao.observeChapter(chapterNumber).map { it?.toDomain() }

    // ── Verses ────────────────────────────────────────────────────────────

    override fun observeVersesForChapter(chapterNumber: Int): Flow<List<Verse>> =
        verseDao.observeVersesForChapter(chapterNumber).map { list ->
            list.map { it.toDomain() }
        }

    override fun observeVerse(verseId: String): Flow<Verse?> =
        verseDao.observeVerse(verseId).map { it?.toDomain() }

    override suspend fun getRandomVerse(): Verse? =
        verseDao.getRandomVerse()?.toDomain()

    // ── Bookmarks ─────────────────────────────────────────────────────────

    override fun observeAllBookmarks(): Flow<List<Bookmark>> =
        bookmarkDao.observeAllBookmarks().map { list ->
            list.map { it.toDomain() }
        }

    override fun observeBookmarkCount(): Flow<Int> =
        bookmarkDao.observeBookmarkCount()

    override suspend fun addBookmark(verseId: String) {
        bookmarkDao.insertBookmark(BookmarkEntity(verseId = verseId, bookmarkedAt = System.currentTimeMillis()))
    }

    override suspend fun removeBookmark(verseId: String) {
        bookmarkDao.deleteBookmark(verseId)
    }

    override suspend fun toggleBookmark(verseId: String) {
        if (bookmarkDao.isBookmarked(verseId)) {
            bookmarkDao.deleteBookmark(verseId)
        } else {
            bookmarkDao.insertBookmark(BookmarkEntity(verseId = verseId, bookmarkedAt = System.currentTimeMillis()))
        }
    }

    // ── Reading Progress ──────────────────────────────────────────────────

    override suspend fun updateReadingProgress(chapterNumber: Int, verseNumber: Int) {
        readingProgressDao.upsertProgress(
            ReadingProgressEntity(
                chapterNumber = chapterNumber,
                lastReadVerseNumber = verseNumber,
                lastReadAt = System.currentTimeMillis()
            )
        )
    }

    override fun observeLastReadChapter(): Flow<ReadingProgress?> =
        readingProgressDao.observeLastReadChapter().map { it?.toDomain() }

    override fun observeOverallProgress(): Flow<Float> =
        readingProgressDao.observeOverallProgress().map { it ?: 0f }

    // ── Search ────────────────────────────────────────────────────────────

    override fun searchVerses(query: String): Flow<List<SearchResult>> =
        verseDao.searchVerses(query).map { list ->
            list.map { verse ->
                SearchResult(
                    verseId = verse.verseId,
                    chapterNumber = verse.chapterNumber,
                    verseNumber = verse.verseNumber,
                    translation = verse.translation,
                    chapterName = "Chapter ${verse.chapterNumber}",
                    matchHighlight = verse.translation.extractHighlight(query)
                )
            }
        }

    // ── Private mappers ───────────────────────────────────────────────────

    private fun ChapterWithProgress.toDomain(): Chapter {
        val progress = if (verseCount > 0) lastReadVerse.toFloat() / verseCount.toFloat() else 0f
        return Chapter(
            number = chapterNumber,
            nameTransliterated = nameTransliterated,
            nameSanskrit = nameSanskrit,
            nameMeaning = nameMeaning,
            summary = summary,
            verseCount = verseCount,
            readingProgressPercent = progress.coerceIn(0f, 1f),
            isStarted = lastReadVerse > 0
        )
    }

    private fun VerseWithBookmark.toDomain(): Verse = Verse(
        id = verseId,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        sanskritText = sanskritText,
        transliteration = transliteration,
        wordMeanings = wordMeanings,
        translation = translation,
        commentary = commentary,
        isBookmarked = isBookmarked
    )

    private fun BookmarkRow.toDomain(): Bookmark = Bookmark(
        verseId = verseId,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        chapterName = chapterName,
        sanskritText = sanskritText,
        translation = translation,
        bookmarkedAt = bookmarkedAt
    )

    private fun ProgressWithTotal.toDomain(): ReadingProgress = ReadingProgress(
        chapterNumber = chapterNumber,
        lastReadVerseNumber = lastReadVerseNumber,
        totalVerses = totalVerses,
        lastReadAt = lastReadAt
    )

    private fun String.extractHighlight(query: String): String {
        val idx = this.indexOf(query, ignoreCase = true)
        if (idx == -1) return this.take(120)
        val start = maxOf(0, idx - 30)
        val end = minOf(length, idx + query.length + 60)
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < length) "…" else ""
        return "$prefix${substring(start, end)}$suffix"
    }
}
