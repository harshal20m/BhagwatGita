package com.gitaapp.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitaapp.core.database.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    /**
     * Upsert reading progress when user navigates to a verse.
     * Uses REPLACE strategy so it acts as both INSERT and UPDATE.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReadingProgressEntity)

    /**
     * Observe progress for a specific chapter.
     */
    @Query("""
        SELECT rp.*, c.verse_count AS totalVerses
        FROM reading_progress rp
        INNER JOIN chapters c ON rp.chapter_number = c.chapter_number
        WHERE rp.chapter_number = :chapterNumber
    """)
    fun observeProgressForChapter(chapterNumber: Int): Flow<ProgressWithTotal?>

    /**
     * Get the last chapter the user was reading — for "Continue Reading" on Home.
     */
    @Query("""
        SELECT rp.*, c.verse_count AS totalVerses
        FROM reading_progress rp
        INNER JOIN chapters c ON rp.chapter_number = c.chapter_number
        ORDER BY rp.last_read_at DESC
        LIMIT 1
    """)
    fun observeLastReadChapter(): Flow<ProgressWithTotal?>

    /**
     * Observe total overall completion percentage.
     */
    @Query("""
        SELECT CAST(SUM(rp.last_read_verse_number) AS FLOAT) / CAST(SUM(c.verse_count) AS FLOAT)
        FROM reading_progress rp
        INNER JOIN chapters c ON rp.chapter_number = c.chapter_number
    """)
    fun observeOverallProgress(): Flow<Float?>
}

/**
 * Progress joined with total verse count.
 */
data class ProgressWithTotal(
    @ColumnInfo(name = "chapter_number")
    val chapterNumber: Int,
    @ColumnInfo(name = "last_read_verse_number")
    val lastReadVerseNumber: Int,
    @ColumnInfo(name = "last_read_at")
    val lastReadAt: Long,
    val totalVerses: Int
)