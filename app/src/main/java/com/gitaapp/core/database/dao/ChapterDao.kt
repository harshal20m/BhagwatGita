package com.gitaapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gitaapp.core.database.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    /**
     * Observe all chapters — emits on every DB change.
     * UI collects this via collectAsStateWithLifecycle().
     */
    @Query("""
        SELECT c.*, 
               COALESCE(rp.last_read_verse_number, 0) AS lastReadVerse
        FROM chapters c
        LEFT JOIN reading_progress rp ON c.chapter_number = rp.chapter_number
        ORDER BY c.chapter_number ASC
    """)
    fun observeAllChapters(): Flow<List<ChapterWithProgress>>

    /**
     * Observe a single chapter by number.
     */
    @Query("""
        SELECT c.*, 
               COALESCE(rp.last_read_verse_number, 0) AS lastReadVerse
        FROM chapters c
        LEFT JOIN reading_progress rp ON c.chapter_number = rp.chapter_number
        WHERE c.chapter_number = :chapterNumber
    """)
    fun observeChapter(chapterNumber: Int): Flow<ChapterWithProgress?>

    /**
     * Insert or replace all chapters — used during database seeding.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    /**
     * Check if chapters are already seeded to avoid re-seeding.
     */
    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun getChapterCount(): Int
}

/**
 * POJO for the JOIN result — not an Entity itself.
 */
data class ChapterWithProgress(
    val chapterNumber: Int,
    val nameTransliterated: String,
    val nameSanskrit: String,
    val nameMeaning: String,
    val summary: String,
    val verseCount: Int,
    val lastReadVerse: Int
)
