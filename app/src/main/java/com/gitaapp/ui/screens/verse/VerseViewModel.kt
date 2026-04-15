package com.gitaapp.ui.screens.verse

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.ReadingPreferences
import com.gitaapp.data.model.Verse
import com.gitaapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface VerseUiState {
    data object Loading : VerseUiState

    @Immutable
    data class Success(
        val verse: Verse,
        val chapterName: String,
        val totalVersesInChapter: Int,
        val preferences: ReadingPreferences
    ) : VerseUiState

    data class Error(val message: String) : VerseUiState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class VerseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val chapterNumber: Int = checkNotNull(
        savedStateHandle[Screen.Verse.ARG_CHAPTER_NUMBER]
    )
    private val verseNumber: Int = checkNotNull(
        savedStateHandle[Screen.Verse.ARG_VERSE_NUMBER]
    )
    private val verseId = "$chapterNumber.$verseNumber"

    val uiState: StateFlow<VerseUiState> = combine(
        repository.observeVerse(verseId),
        repository.observeChapter(chapterNumber),
        preferencesManager.readingPreferences
    ) { verse, chapter, prefs ->
        when {
            verse == null || chapter == null -> VerseUiState.Loading
            else -> VerseUiState.Success(
                verse = verse,
                chapterName = chapter.nameTransliterated,
                totalVersesInChapter = chapter.verseCount,
                preferences = prefs
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VerseUiState.Loading
    )

    init {
        // Record reading progress as soon as the verse is opened
        viewModelScope.launch {
            repository.updateReadingProgress(chapterNumber, verseNumber)
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            repository.toggleBookmark(verseId)
        }
    }

    fun navigateToVerse(targetVerseNumber: Int): String =
        "$chapterNumber.$targetVerseNumber"
}
