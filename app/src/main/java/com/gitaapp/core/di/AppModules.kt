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
import com.gitaapp.notification.NotificationScheduler
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
        Room.databaseBuilder(context, GitaDatabase::class.java, GitaDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideChapterDao(db: GitaDatabase): ChapterDao = db.chapterDao()
    @Provides fun provideVerseDao(db: GitaDatabase): VerseDao = db.verseDao()
    @Provides fun provideBookmarkDao(db: GitaDatabase): BookmarkDao = db.bookmarkDao()
    @Provides fun provideReadingProgressDao(db: GitaDatabase): ReadingProgressDao = db.readingProgressDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    /** Provide raw Context for NotificationScheduler. */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGitaRepository(impl: GitaRepositoryImpl): GitaRepository
}
