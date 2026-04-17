package com.gitaapp.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gitaapp.data.model.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gita_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}

@Singleton
class PreferencesManager @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val FONT_SIZE              = stringPreferencesKey("font_size")
        val SHOW_TRANSLITERATION   = booleanPreferencesKey("show_transliteration")
        val SHOW_WORD_MEANINGS     = booleanPreferencesKey("show_word_meanings")
        val SHOW_COMMENTARY        = booleanPreferencesKey("show_commentary")
        val THEME_MODE             = stringPreferencesKey("theme_mode")
        val APP_THEME              = stringPreferencesKey("app_theme")
        val LANGUAGE               = stringPreferencesKey("language")
        val FOCUS_MODE             = booleanPreferencesKey("focus_mode")
        val DAILY_REMINDER         = booleanPreferencesKey("daily_reminder")
        val REMINDER_HOUR          = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE        = intPreferencesKey("reminder_minute")
        val AUTO_NEXT_ENABLED      = booleanPreferencesKey("auto_next_enabled")
        val AUTO_NEXT_INTERVAL     = intPreferencesKey("auto_next_interval")
        val SHOW_LIFE_INDICATOR    = booleanPreferencesKey("show_life_indicator")
        val LIFE_MAX_YEARS         = intPreferencesKey("life_max_years")
        val SHOW_LIFE_DAYS          = booleanPreferencesKey("show_life_days")
        val SHOW_LIFE_MONTHS        = booleanPreferencesKey("show_life_months")
        val SHOW_LIFE_YEARS         = booleanPreferencesKey("show_life_years")
        val SEARCH_HISTORY         = stringPreferencesKey("search_history")
        val PROFILE_NAME           = stringPreferencesKey("profile_name")
        val PROFILE_DOB_DAY        = intPreferencesKey("profile_dob_day")
        val PROFILE_DOB_MONTH      = intPreferencesKey("profile_dob_month")
        val PROFILE_DOB_YEAR       = intPreferencesKey("profile_dob_year")
        val PROFILE_BIRTH_HOUR     = intPreferencesKey("profile_birth_hour")
        val PROFILE_BIRTH_MINUTE   = intPreferencesKey("profile_birth_minute")
        val PROFILE_IS_SETUP       = booleanPreferencesKey("profile_is_setup")
        val PROFILE_MANUAL_RASHI   = intPreferencesKey("profile_manual_rashi")
        val VOTD_VERSE_ID          = stringPreferencesKey("votd_verse_id")
        val VOTD_LAST_UPDATE_DAY   = longPreferencesKey("votd_last_update_day")
    }

    val readingPreferences: Flow<ReadingPreferences> = dataStore.data.map { p ->
        ReadingPreferences(
            fontSize             = safeEnum(p[Keys.FONT_SIZE], FontSize.MEDIUM),
            showTransliteration  = p[Keys.SHOW_TRANSLITERATION] ?: true,
            showWordMeanings     = p[Keys.SHOW_WORD_MEANINGS] ?: true,
            showCommentary       = p[Keys.SHOW_COMMENTARY] ?: true,
            themeMode            = safeEnum(p[Keys.THEME_MODE], ThemeMode.SYSTEM),
            language             = safeEnum(p[Keys.LANGUAGE], AppLanguage.HINDI),
            focusModeEnabled     = p[Keys.FOCUS_MODE] ?: false,
            dailyReminderEnabled = p[Keys.DAILY_REMINDER] ?: false,
            reminderHour         = p[Keys.REMINDER_HOUR] ?: 7,
            reminderMinute       = p[Keys.REMINDER_MINUTE] ?: 0,
            autoNextEnabled      = p[Keys.AUTO_NEXT_ENABLED] ?: false,
            autoNextIntervalSeconds = p[Keys.AUTO_NEXT_INTERVAL] ?: 15,
            showLifeIndicator    = p[Keys.SHOW_LIFE_INDICATOR] ?: false,
            lifeIndicatorMaxYears = p[Keys.LIFE_MAX_YEARS] ?: 90,
            showLifeDays         = p[Keys.SHOW_LIFE_DAYS] ?: true,
            showLifeMonths       = p[Keys.SHOW_LIFE_MONTHS] ?: true,
            showLifeYears        = p[Keys.SHOW_LIFE_YEARS] ?: true
        )
    }

    val appTheme: Flow<AppTheme> = dataStore.data.map { p ->
        safeEnum(p[Keys.APP_THEME], AppTheme.SAFFRON)
    }

    val userProfile: Flow<UserProfile> = dataStore.data.map { p ->
        UserProfile(
            name        = p[Keys.PROFILE_NAME] ?: "",
            dobDay      = p[Keys.PROFILE_DOB_DAY] ?: 1,
            dobMonth    = p[Keys.PROFILE_DOB_MONTH] ?: 1,
            dobYear     = p[Keys.PROFILE_DOB_YEAR] ?: 2000,
            birthHour   = p[Keys.PROFILE_BIRTH_HOUR] ?: 12,
            birthMinute = p[Keys.PROFILE_BIRTH_MINUTE] ?: 0,
            manualRashiIndex = p[Keys.PROFILE_MANUAL_RASHI],
            isSetup     = p[Keys.PROFILE_IS_SETUP] ?: false
        )
    }

    val searchHistory: Flow<List<String>> = dataStore.data.map { p ->
        (p[Keys.SEARCH_HISTORY] ?: "").split("|").filter { it.isNotBlank() }.take(10)
    }

    val verseOfTheDayId: Flow<String?> = dataStore.data.map { it[Keys.VOTD_VERSE_ID] }
    val verseOfTheDayLastUpdate: Flow<Long> = dataStore.data.map { it[Keys.VOTD_LAST_UPDATE_DAY] ?: 0L }

    // ── Setters ───────────────────────────────────────────────────────────────
    suspend fun setVerseOfTheDay(verseId: String, day: Long) {
        dataStore.edit {
            it[Keys.VOTD_VERSE_ID] = verseId
            it[Keys.VOTD_LAST_UPDATE_DAY] = day
        }
    }
    suspend fun setFontSize(v: FontSize)            { dataStore.edit { it[Keys.FONT_SIZE] = v.name } }
    suspend fun setShowTransliteration(v: Boolean)  { dataStore.edit { it[Keys.SHOW_TRANSLITERATION] = v } }
    suspend fun setShowWordMeanings(v: Boolean)      { dataStore.edit { it[Keys.SHOW_WORD_MEANINGS] = v } }
    suspend fun setShowCommentary(v: Boolean)        { dataStore.edit { it[Keys.SHOW_COMMENTARY] = v } }
    suspend fun setThemeMode(v: ThemeMode)           { dataStore.edit { it[Keys.THEME_MODE] = v.name } }
    suspend fun setAppTheme(v: AppTheme)             { dataStore.edit { it[Keys.APP_THEME] = v.name } }
    @Inject @ApplicationContext lateinit var context: Context

    suspend fun setLanguage(v: AppLanguage) {
        dataStore.edit { it[Keys.LANGUAGE] = v.name }
        // Update widget language preference
        try {
            com.gitaapp.widget.VerseOfDayWidgetUpdater.updateLanguage(context, v.name)
        } catch (e: Exception) { }
    }
    suspend fun setFocusMode(v: Boolean)             { dataStore.edit { it[Keys.FOCUS_MODE] = v } }
    suspend fun setDailyReminder(v: Boolean)         { dataStore.edit { it[Keys.DAILY_REMINDER] = v } }
    suspend fun setReminderTime(h: Int, m: Int)      { dataStore.edit { it[Keys.REMINDER_HOUR] = h; it[Keys.REMINDER_MINUTE] = m } }
    suspend fun setAutoNext(v: Boolean)              { dataStore.edit { it[Keys.AUTO_NEXT_ENABLED] = v } }
    suspend fun setAutoNextInterval(seconds: Int)    { dataStore.edit { it[Keys.AUTO_NEXT_INTERVAL] = seconds } }
    suspend fun setShowLifeIndicator(v: Boolean)     { 
        dataStore.edit { it[Keys.SHOW_LIFE_INDICATOR] = v }
        try {
            (context.applicationContext as? com.gitaapp.GitaApplication)?.syncLifeWidget()
        } catch (e: Exception) {}
    }

    suspend fun setLifeMaxYears(v: Int) { dataStore.edit { it[Keys.LIFE_MAX_YEARS] = v } }
    suspend fun setShowLifeDays(v: Boolean) { dataStore.edit { it[Keys.SHOW_LIFE_DAYS] = v } }
    suspend fun setShowLifeMonths(v: Boolean) { dataStore.edit { it[Keys.SHOW_LIFE_MONTHS] = v } }
    suspend fun setShowLifeYears(v: Boolean) { dataStore.edit { it[Keys.SHOW_LIFE_YEARS] = v } }

    suspend fun saveProfile(name: String, day: Int, month: Int, year: Int, hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.PROFILE_NAME]         = name
            it[Keys.PROFILE_DOB_DAY]      = day
            it[Keys.PROFILE_DOB_MONTH]    = month
            it[Keys.PROFILE_DOB_YEAR]     = year
            it[Keys.PROFILE_BIRTH_HOUR]   = hour
            it[Keys.PROFILE_BIRTH_MINUTE] = minute
            it[Keys.PROFILE_IS_SETUP]     = true
        }
        try {
            (context.applicationContext as? com.gitaapp.GitaApplication)?.syncLifeWidget()
        } catch (e: Exception) {}
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank() || query.length < 2) return
        dataStore.edit { p ->
            val prev = (p[Keys.SEARCH_HISTORY] ?: "").split("|").filter { it.isNotBlank() && it != query }
            p[Keys.SEARCH_HISTORY] = (listOf(query) + prev).take(10).joinToString("|")
        }
    }

    suspend fun removeSearchHistory(query: String) {
        dataStore.edit { p ->
            val prev = (p[Keys.SEARCH_HISTORY] ?: "").split("|").filter { it.isNotBlank() }
            p[Keys.SEARCH_HISTORY] = prev.filter { it != query }.joinToString("|")
        }
    }

    suspend fun clearSearchHistory() { dataStore.edit { it[Keys.SEARCH_HISTORY] = "" } }

    suspend fun setManualRashi(index: Int?) {
        dataStore.edit { p ->
            if (index == null) p.remove(Keys.PROFILE_MANUAL_RASHI)
            else p[Keys.PROFILE_MANUAL_RASHI] = index
        }
    }

    private inline fun <reified T : Enum<T>> safeEnum(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}