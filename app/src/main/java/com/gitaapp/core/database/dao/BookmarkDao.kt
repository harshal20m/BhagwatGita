package com.gitaapp.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitaapp.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("""
        SELECT b.verse_id, v.chapter_number, v.verse_number,
               c.name_transliterated AS chapter_name,
               v.sanskrit_text, v.translation, b.bookmarked_at
        FROM bookmarks b
        INNER JOIN verses v   ON b.verse_id = v.verse_id
        INNER JOIN chapters c ON v.chapter_number = c.chapter_number
        ORDER BY b.bookmarked_at DESC
    """)
    fun observeAllBookmarks(): Flow<List<BookmarkRow>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verse_id = :verseId")
    suspend fun deleteBookmark(verseId: String)

    @Query("SELECT COUNT(*) > 0 FROM bookmarks WHERE verse_id = :verseId")
    suspend fun isBookmarked(verseId: String): Boolean

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun observeBookmarkCount(): Flow<Int>
}

data class BookmarkRow(
    @ColumnInfo(name = "verse_id")             val verseId: String,
    @ColumnInfo(name = "chapter_number")       val chapterNumber: Int,
    @ColumnInfo(name = "verse_number")         val verseNumber: Int,
    @ColumnInfo(name = "chapter_name")         val chapterName: String,
    @ColumnInfo(name = "sanskrit_text")        val sanskritText: String,
    @ColumnInfo(name = "translation")          val translation: String,
    @ColumnInfo(name = "bookmarked_at")        val bookmarkedAt: Long
)
