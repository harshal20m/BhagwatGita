package com.gitaapp.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gitaapp.data.model.FontSize
import com.gitaapp.data.model.ReadingPreferences
import com.gitaapp.data.model.ThemeMode
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

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}

/**
 * Manages user reading preferences via DataStore.
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        val SHOW_WORD_MEANINGS = booleanPreferencesKey("show_word_meanings")
        val SHOW_COMMENTARY = booleanPreferencesKey("show_commentary")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val readingPreferences: Flow<ReadingPreferences> = dataStore.data.map { prefs ->
        ReadingPreferences(
            fontSize = FontSize.valueOf(prefs[Keys.FONT_SIZE] ?: FontSize.MEDIUM.name),
            showTransliteration = prefs[Keys.SHOW_TRANSLITERATION] ?: true,
            showWordMeanings = prefs[Keys.SHOW_WORD_MEANINGS] ?: true,
            showCommentary = prefs[Keys.SHOW_COMMENTARY] ?: true,
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        )
    }

    suspend fun setFontSize(fontSize: FontSize) {
        dataStore.edit { it[Keys.FONT_SIZE] = fontSize.name }
    }

    suspend fun setShowTransliteration(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_TRANSLITERATION] = show }
    }

    suspend fun setShowWordMeanings(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_WORD_MEANINGS] = show }
    }

    suspend fun setShowCommentary(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_COMMENTARY] = show }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
