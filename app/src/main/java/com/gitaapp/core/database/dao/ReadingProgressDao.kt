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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReadingProgressEntity)

    @Query("""
        SELECT rp.chapter_number, rp.last_read_verse_number,
               rp.last_read_at, c.verse_count AS total_verses
        FROM reading_progress rp
        INNER JOIN chapters c ON rp.chapter_number = c.chapter_number
        WHERE rp.chapter_number = :chapterNumber
    """)
    fun observeProgressForChapter(chapterNumber: Int): Flow<ProgressWithTotal?>

    @Query("""
        SELECT rp.chapter_number, rp.last_read_verse_number,
               rp.last_read_at, c.verse_count AS total_verses
        FROM reading_progress rp
        INNER JOIN chapters c ON rp.chapter_number = c.chapter_number
        ORDER BY rp.last_read_at DESC
        LIMIT 1
    """)
    fun observeLastReadChapter(): Flow<ProgressWithTotal?>

    @Query("""
        SELECT CAST(SUM(rp.last_read_verse_number) AS FLOAT) /
               CAST(SUM(c.verse_count) AS FLOAT)
        FROM reading_progress rp
        INNER JOIN chapters c ON rp.chapter_number = c.chapter_number
    """)
    fun observeOverallProgress(): Flow<Float?>
}

data class ProgressWithTotal(
    @ColumnInfo(name = "chapter_number")       val chapterNumber: Int,
    @ColumnInfo(name = "last_read_verse_number") val lastReadVerseNumber: Int,
    @ColumnInfo(name = "last_read_at")         val lastReadAt: Long,
    @ColumnInfo(name = "total_verses")         val totalVerses: Int
)
