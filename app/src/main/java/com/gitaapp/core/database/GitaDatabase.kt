package com.gitaapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gitaapp.core.database.dao.BookmarkDao
import com.gitaapp.core.database.dao.ChapterDao
import com.gitaapp.core.database.dao.ReadingProgressDao
import com.gitaapp.core.database.dao.VerseDao
import com.gitaapp.core.database.entity.BookmarkEntity
import com.gitaapp.core.database.entity.ChapterEntity
import com.gitaapp.core.database.entity.ReadingProgressEntity
import com.gitaapp.core.database.entity.VerseEntity

/**
 * Central Room database for the Bhagavad Gita app.
 *
 * Version 1 — baseline schema.
 * Migrations must be added to [GitaDatabase.MIGRATIONS] array for version bumps.
 *
 * exportSchema = true — generates schema JSON files to /schemas/ for migration verification.
 */
@Database(
    entities = [
        ChapterEntity::class,
        VerseEntity::class,
        BookmarkEntity::class,
        ReadingProgressEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class GitaDatabase : RoomDatabase() {

    abstract fun chapterDao(): ChapterDao
    abstract fun verseDao(): VerseDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingProgressDao(): ReadingProgressDao

    companion object {
        const val DATABASE_NAME = "gita_database"
    }
}