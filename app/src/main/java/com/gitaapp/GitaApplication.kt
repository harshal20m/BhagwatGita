package com.gitaapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.notification.NotificationScheduler
import com.gitaapp.widget.VerseOfDayWidgetUpdater
import com.gitaapp.worker.SeedDatabaseWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point.
 *
 * On first install: seeds Room database from bundled JSON assets via WorkManager.
 * On every launch:  schedules (or cancels) the daily notification based on user prefs,
 *                   and refreshes any active home-screen widgets with a fresh verse.
 */
@HiltAndroidApp
class GitaApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var repository: GitaRepository

    /** Application-scoped coroutine scope that outlives any individual ViewModel. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDatabaseSeed()
        applyNotificationPreferences()
        refreshWidget()
    }

    // ── One-shot DB seed ──────────────────────────────────────────────────────

    private fun scheduleDatabaseSeed() {
        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
            .setConstraints(Constraints.NONE)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            SeedDatabaseWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    // ── Notification scheduling ───────────────────────────────────────────────

    /**
     * Reads the user's saved notification preferences and either schedules or
     * cancels the daily reminder accordingly.  Runs once on each app launch so
     * the schedule always reflects the latest saved time.
     */
    private fun applyNotificationPreferences() {
        appScope.launch {
            val prefs = preferencesManager.readingPreferences.first()
            if (prefs.dailyReminderEnabled) {
                notificationScheduler.schedule(prefs.reminderHour, prefs.reminderMinute)
            } else {
                notificationScheduler.cancel()
            }
        }
    }

    // ── Widget refresh ────────────────────────────────────────────────────────

    /**
     * Pushes a fresh random verse into all active Verse-of-the-Day widget instances.
     * Called on launch so the widget is always up to date without waiting for the
     * next system appwidget update broadcast.
     */
    private fun refreshWidget() {
        appScope.launch {
            val verse = repository.getRandomVerse() ?: return@launch
            VerseOfDayWidgetUpdater.update(
                context     = this@GitaApplication,
                verseRef    = "${verse.chapterNumber}.${verse.verseNumber}",
                sanskrit    = verse.sanskritText,
                translation = verse.translation
            )
        }
    }
}
