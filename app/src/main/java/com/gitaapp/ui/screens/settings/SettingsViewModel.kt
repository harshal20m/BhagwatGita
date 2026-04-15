package com.gitaapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.data.model.FontSize
import com.gitaapp.data.model.ReadingPreferences
import com.gitaapp.data.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val fontSize: FontSize = FontSize.MEDIUM,
    val showTransliteration: Boolean = true,
    val showWordMeanings: Boolean = true,
    val showCommentary: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferencesManager.readingPreferences
        .map { prefs ->
            SettingsUiState(
                fontSize = prefs.fontSize,
                showTransliteration = prefs.showTransliteration,
                showWordMeanings = prefs.showWordMeanings,
                showCommentary = prefs.showCommentary,
                themeMode = prefs.themeMode
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun setFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            preferencesManager.setFontSize(fontSize)
        }
    }

    fun setShowTransliteration(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowTransliteration(show)
        }
    }

    fun setShowWordMeanings(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowWordMeanings(show)
        }
    }

    fun setShowCommentary(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowCommentary(show)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }
}
