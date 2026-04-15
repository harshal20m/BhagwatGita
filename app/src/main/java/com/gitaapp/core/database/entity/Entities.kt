package com.gitaapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a Bhagavad Gita chapter.
 */
@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey
    @ColumnInfo(name = "chapter_number")
    val chapterNumber: Int,

    @ColumnInfo(name = "name_transliterated")
    val nameTransliterated: String,

    @ColumnInfo(name = "name_sanskrit")
    val nameSanskrit: String,

    @ColumnInfo(name = "name_meaning")
    val nameMeaning: String,

    @ColumnInfo(name = "summary")
    val summary: String,

    @ColumnInfo(name = "verse_count")
    val verseCount: Int
)

/**
 * Room entity for a single verse (shloka).
 * Uses a composite natural key: chapterNumber + verseNumber.
 */
@Entity(
    tableName = "verses",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["chapter_number"],
            childColumns = ["chapter_number"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chapter_number"]),
        Index(value = ["chapter_number", "verse_number"], unique = true)
    ]
)
data class VerseEntity(
    @PrimaryKey
    @ColumnInfo(name = "verse_id")
    val verseId: String,  // Format: "chapterNumber.verseNumber" e.g. "1.1"

    @ColumnInfo(name = "chapter_number")
    val chapterNumber: Int,

    @ColumnInfo(name = "verse_number")
    val verseNumber: Int,

    @ColumnInfo(name = "sanskrit_text")
    val sanskritText: String,

    @ColumnInfo(name = "transliteration")
    val transliteration: String,

    @ColumnInfo(name = "word_meanings")
    val wordMeanings: String,

    @ColumnInfo(name = "translation")
    val translation: String,

    @ColumnInfo(name = "commentary")
    val commentary: String
)

/**
 * Room entity for bookmarked verses.
 */
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = VerseEntity::class,
            parentColumns = ["verse_id"],
            childColumns = ["verse_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["verse_id"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey
    @ColumnInfo(name = "verse_id")
    val verseId: String,

    @ColumnInfo(name = "bookmarked_at")
    val bookmarkedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity tracking reading progress per chapter.
 */
@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["chapter_number"],
            childColumns = ["chapter_number"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chapter_number"], unique = true)]
)
data class ReadingProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "chapter_number")
    val chapterNumber: Int,

    @ColumnInfo(name = "last_read_verse_number")
    val lastReadVerseNumber: Int,

    @ColumnInfo(name = "last_read_at")
    val lastReadAt: Long = System.currentTimeMillis()
)
