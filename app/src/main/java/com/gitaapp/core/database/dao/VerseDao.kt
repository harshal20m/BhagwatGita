package com.gitaapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitaapp.core.database.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {

    /**
     * Observe all verses for a chapter, joined with bookmark status.
     */
    @Query("""
        SELECT v.*, 
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS isBookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        WHERE v.chapter_number = :chapterNumber
        ORDER BY v.verse_number ASC
    """)
    fun observeVersesForChapter(chapterNumber: Int): Flow<List<VerseWithBookmark>>

    /**
     * Observe a single verse by its ID.
     */
    @Query("""
        SELECT v.*, 
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS isBookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        WHERE v.verse_id = :verseId
    """)
    fun observeVerse(verseId: String): Flow<VerseWithBookmark?>

    /**
     * Insert all verses — used during database seeding.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(verses: List<VerseEntity>)

    /**
     * Full-text search across translation and commentary.
     * Uses LIKE for compatibility — for production scale, consider FTS5.
     */
    @Query("""
        SELECT v.*, 
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS isBookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        WHERE v.translation LIKE '%' || :query || '%'
           OR v.commentary LIKE '%' || :query || '%'
           OR v.transliteration LIKE '%' || :query || '%'
        ORDER BY v.chapter_number ASC, v.verse_number ASC
        LIMIT 50
    """)
    fun searchVerses(query: String): Flow<List<VerseWithBookmark>>

    /**
     * Get a random verse — for "Verse of the Day" feature.
     */
    @Query("""
        SELECT v.*, 
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS isBookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomVerse(): VerseWithBookmark?
}

/**
 * POJO for verse joined with bookmark status.
 */
data class VerseWithBookmark(
    val verseId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val sanskritText: String,
    val transliteration: String,
    val wordMeanings: String,
    val translation: String,
    val commentary: String,
    val isBookmarked: Boolean
)
