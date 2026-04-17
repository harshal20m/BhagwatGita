package com.gitaapp

import android.app.Application
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.notification.NotificationScheduler
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.gitaapp.widget.LifeIndicatorWidget
import com.gitaapp.widget.VerseOfDayWidget
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
 */
@HiltAndroidApp
class GitaApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var repository: GitaRepository

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
        refreshWidget(force = false)
        syncLifeWidget()
    }

    fun syncLifeWidget() {
        appScope.launch {
            val profile = preferencesManager.userProfile.first()
            val prefs = preferencesManager.readingPreferences.first()
            val manager = GlanceAppWidgetManager(this@GitaApplication)
            val ids = manager.getGlanceIds(LifeIndicatorWidget::class.java)
            if (ids.isEmpty()) return@launch

            ids.forEach { id ->
                updateAppWidgetState(this@GitaApplication, PreferencesGlanceStateDefinition, id) { p ->
                    p.toMutablePreferences().apply {
                        this[LifeIndicatorWidget.PROFILE_NAME] = profile.name
                        this[LifeIndicatorWidget.PROFILE_DOB_DAY] = profile.dobDay
                        this[LifeIndicatorWidget.PROFILE_DOB_MONTH] = profile.dobMonth
                        this[LifeIndicatorWidget.PROFILE_DOB_YEAR] = profile.dobYear
                        this[LifeIndicatorWidget.LANGUAGE] = prefs.language.name
                    }
                }
            }
            LifeIndicatorWidget().updateAll(this@GitaApplication)
        }
    }

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

    fun refreshWidget(force: Boolean = false) {
        appScope.launch {
            val prefs = preferencesManager.readingPreferences.first()
            val today = java.util.Calendar.getInstance().let {
                it.set(java.util.Calendar.HOUR_OF_DAY, 0)
                it.set(java.util.Calendar.MINUTE, 0)
                it.set(java.util.Calendar.SECOND, 0)
                it.set(java.util.Calendar.MILLISECOND, 0)
                it.timeInMillis
            }

            if (!force) {
                // Check if we already have a verse for today in preferences
                val savedVerseId = preferencesManager.verseOfTheDayId.first()
                val lastUpdate = preferencesManager.verseOfTheDayLastUpdate.first()

                if (savedVerseId != null && lastUpdate == today) {
                    val verse = repository.observeVerse(savedVerseId).first()
                    val chapter = verse?.let { repository.observeChapter(it.chapterNumber).first() }
                    
                    if (verse != null) {
                        VerseOfDayWidgetUpdater.update(
                            context       = this@GitaApplication,
                            verseRef      = verse.id,
                            chapterName   = chapter?.nameTransliterated ?: "Chapter ${verse.chapterNumber}",
                            sanskrit      = verse.sanskritText,
                            translationEn = verse.translation,
                            translationHi = verse.translationHi,
                            language      = prefs.language.name,
                            isBookmarked  = verse.isBookmarked,
                            updateDay     = today
                        )
                        return@launch
                    }
                }
            }

            val verse = repository.getRandomVerse() ?: return@launch
            val chapter = repository.observeChapter(verse.chapterNumber).first()
            
            // Save to preferences so Home Screen and Widget stay in sync
            preferencesManager.setVerseOfTheDay(verse.id, today)
            
            VerseOfDayWidgetUpdater.update(
                context       = this@GitaApplication,
                verseRef      = verse.id,
                chapterName   = chapter?.nameTransliterated ?: "Chapter ${verse.chapterNumber}",
                sanskrit      = verse.sanskritText,
                translationEn = verse.translation,
                translationHi = verse.translationHi,
                language      = prefs.language.name,
                isBookmarked  = verse.isBookmarked,
                updateDay     = today
            )
        }
    }

    fun toggleWidgetBookmark() {
        appScope.launch {
            val manager = GlanceAppWidgetManager(this@GitaApplication)
            val ids = manager.getGlanceIds(VerseOfDayWidget::class.java)
            if (ids.isEmpty()) return@launch
            
            val state = getAppWidgetState(
                this@GitaApplication,
                PreferencesGlanceStateDefinition,
                ids.first()
            )
            val verseRef = state[VerseOfDayWidget.KEY_VERSE_REF] ?: return@launch
            
            repository.toggleBookmark(verseRef)
            val verse = repository.observeVerse(verseRef).first()
            
            if (verse != null) {
                ids.forEach { id ->
                    updateAppWidgetState(
                        this@GitaApplication,
                        PreferencesGlanceStateDefinition,
                        id
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[VerseOfDayWidget.KEY_IS_BOOKMARKED] = verse.isBookmarked
                        }
                    }
                }
                VerseOfDayWidget().updateAll(this@GitaApplication)
            }
        }
    }
}