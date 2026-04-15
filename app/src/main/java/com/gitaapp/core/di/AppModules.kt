package com.gitaapp.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.gitaapp.core.database.GitaDatabase
import com.gitaapp.core.database.dao.BookmarkDao
import com.gitaapp.core.database.dao.ChapterDao
import com.gitaapp.core.database.dao.ReadingProgressDao
import com.gitaapp.core.database.dao.VerseDao
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.core.repository.impl.GitaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGitaDatabase(@ApplicationContext context: Context): GitaDatabase =
        Room.databaseBuilder(
            context,
            GitaDatabase::class.java,
            GitaDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Replace with proper migrations in production
            .build()

    @Provides
    fun provideChapterDao(database: GitaDatabase): ChapterDao =
        database.chapterDao()

    @Provides
    fun provideVerseDao(database: GitaDatabase): VerseDao =
        database.verseDao()

    @Provides
    fun provideBookmarkDao(database: GitaDatabase): BookmarkDao =
        database.bookmarkDao()

    @Provides
    fun provideReadingProgressDao(database: GitaDatabase): ReadingProgressDao =
        database.readingProgressDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGitaRepository(impl: GitaRepositoryImpl): GitaRepository
}
