package com.gitaapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitaapp.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    /**
     * Observe all bookmarked verses with chapter context, newest first.
     */
    @Query("""
        SELECT b.verse_id AS verseId,
               v.chapter_number AS chapterNumber,
               v.verse_number AS verseNumber,
               c.name_transliterated AS chapterName,
               v.sanskrit_text AS sanskritText,
               v.translation AS translation,
               b.bookmarked_at AS bookmarkedAt
        FROM bookmarks b
        INNER JOIN verses v ON b.verse_id = v.verse_id
        INNER JOIN chapters c ON v.chapter_number = c.chapter_number
        ORDER BY b.bookmarked_at DESC
    """)
    fun observeAllBookmarks(): Flow<List<BookmarkRow>>

    /**
     * Insert a [BookmarkEntity] — IGNORE conflict means add-only,
     * duplicate inserts are silently dropped.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    /**
     * Remove a bookmark.
     */
    @Query("DELETE FROM bookmarks WHERE verse_id = :verseId")
    suspend fun deleteBookmark(verseId: String)

    /**
     * Toggle bookmark state — used for single-tap bookmark icon.
     */
    @Query("SELECT COUNT(*) > 0 FROM bookmarks WHERE verse_id = :verseId")
    suspend fun isBookmarked(verseId: String): Boolean

    /**
     * Total bookmark count for Home screen stat.
     */
    @Query("SELECT COUNT(*) FROM bookmarks")
    fun observeBookmarkCount(): Flow<Int>
}

/**
 * Raw query result POJO for the bookmark list.
 */
data class BookmarkRow(
    val verseId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val chapterName: String,
    val sanskritText: String,
    val translation: String,
    val bookmarkedAt: Long
)
