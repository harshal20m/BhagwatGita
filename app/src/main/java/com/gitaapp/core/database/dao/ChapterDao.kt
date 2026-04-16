package com.gitaapp.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitaapp.core.database.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    @Query("""
        SELECT c.chapter_number, c.name_transliterated, c.name_sanskrit,
               c.name_meaning, c.summary, c.verse_count,
               COALESCE(rp.last_read_verse_number, 0) AS last_read_verse
        FROM chapters c
        LEFT JOIN reading_progress rp ON c.chapter_number = rp.chapter_number
        ORDER BY c.chapter_number ASC
    """)
    fun observeAllChapters(): Flow<List<ChapterWithProgress>>

    @Query("""
        SELECT c.chapter_number, c.name_transliterated, c.name_sanskrit,
               c.name_meaning, c.summary, c.verse_count,
               COALESCE(rp.last_read_verse_number, 0) AS last_read_verse
        FROM chapters c
        LEFT JOIN reading_progress rp ON c.chapter_number = rp.chapter_number
        WHERE c.chapter_number = :chapterNumber
    """)
    fun observeChapter(chapterNumber: Int): Flow<ChapterWithProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun getChapterCount(): Int
}

data class ChapterWithProgress(
    @ColumnInfo(name = "chapter_number")      val chapterNumber: Int,
    @ColumnInfo(name = "name_transliterated") val nameTransliterated: String,
    @ColumnInfo(name = "name_sanskrit")       val nameSanskrit: String,
    @ColumnInfo(name = "name_meaning")        val nameMeaning: String,
    @ColumnInfo(name = "summary")             val summary: String,
    @ColumnInfo(name = "verse_count")         val verseCount: Int,
    @ColumnInfo(name = "last_read_verse")     val lastReadVerse: Int
)
