package com.gitaapp.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitaapp.core.database.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {

    @Query("""
        SELECT v.verse_id, v.chapter_number, v.verse_number,
               v.sanskrit_text, v.transliteration, v.word_meanings,
               v.translation, v.translation_hi, v.commentary,
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        WHERE v.chapter_number = :chapterNumber
        ORDER BY v.verse_number ASC
    """)
    fun observeVersesForChapter(chapterNumber: Int): Flow<List<VerseWithBookmark>>

    @Query("""
        SELECT v.verse_id, v.chapter_number, v.verse_number,
               v.sanskrit_text, v.transliteration, v.word_meanings,
               v.translation, v.translation_hi, v.commentary,
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        WHERE v.verse_id = :verseId
    """)
    fun observeVerse(verseId: String): Flow<VerseWithBookmark?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(verses: List<VerseEntity>)

    @Query("""
        SELECT v.verse_id, v.chapter_number, v.verse_number,
               v.sanskrit_text, v.transliteration, v.word_meanings,
               v.translation, v.translation_hi, v.commentary,
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        WHERE v.translation LIKE '%' || :query || '%'
           OR v.translation_hi LIKE '%' || :query || '%'
           OR v.commentary  LIKE '%' || :query || '%'
           OR v.transliteration LIKE '%' || :query || '%'
        ORDER BY v.chapter_number ASC, v.verse_number ASC
        LIMIT 50
    """)
    fun searchVerses(query: String): Flow<List<VerseWithBookmark>>

    @Query("""
        SELECT v.verse_id, v.chapter_number, v.verse_number,
               v.sanskrit_text, v.transliteration, v.word_meanings,
               v.translation, v.translation_hi, v.commentary,
               CASE WHEN b.verse_id IS NOT NULL THEN 1 ELSE 0 END AS is_bookmarked
        FROM verses v
        LEFT JOIN bookmarks b ON v.verse_id = b.verse_id
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomVerse(): VerseWithBookmark?

    @Query("SELECT COUNT(*) FROM verses")
    fun observeVerseCount(): Flow<Int>
}

data class VerseWithBookmark(
    @ColumnInfo(name = "verse_id")       val verseId: String,
    @ColumnInfo(name = "chapter_number") val chapterNumber: Int,
    @ColumnInfo(name = "verse_number")   val verseNumber: Int,
    @ColumnInfo(name = "sanskrit_text")  val sanskritText: String,
    @ColumnInfo(name = "transliteration")val transliteration: String,
    @ColumnInfo(name = "word_meanings")  val wordMeanings: String,
    @ColumnInfo(name = "translation")    val translation: String,
    @ColumnInfo(name = "translation_hi") val translationHi: String,
    @ColumnInfo(name = "commentary")     val commentary: String,
    @ColumnInfo(name = "is_bookmarked")  val isBookmarked: Boolean
)