package com.gitaapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gitaapp.worker.SeedDatabaseWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class wired up with Hilt and WorkManager.
 *
 * On every first-install launch (or after data clear), [SeedDatabaseWorker]
 * reads gita_chapters.json and gita_verses.json from assets and populates Room.
 * The worker is idempotent — it skips seeding if chapters already exist.
 */
@HiltAndroidApp
class GitaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDatabaseSeed()
    }

    /**
     * Enqueue the one-shot seed worker with KEEP policy so it only
     * runs once per install and is never duplicated if the app is killed
     * mid-seeding — WorkManager will restart it automatically.
     */
    private fun scheduleDatabaseSeed() {
        val seedRequest = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            SeedDatabaseWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,   // Don't re-enqueue if pending or running
            seedRequest
        )
    }
}
