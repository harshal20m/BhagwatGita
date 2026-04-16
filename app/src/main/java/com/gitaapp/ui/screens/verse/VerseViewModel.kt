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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class VerseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val chapterNumber: Int = checkNotNull(savedStateHandle[Screen.Verse.ARG_CHAPTER_NUMBER])
    private val verseNumber: Int   = checkNotNull(savedStateHandle[Screen.Verse.ARG_VERSE_NUMBER])
    private val verseId = "$chapterNumber.$verseNumber"

    val uiState: StateFlow<VerseUiState> = combine(
        repository.observeVerse(verseId),
        repository.observeChapter(chapterNumber),
        preferencesManager.readingPreferences
    ) { verse, chapter, prefs ->
        if (verse == null || chapter == null) VerseUiState.Loading
        else VerseUiState.Success(verse, chapter.nameTransliterated, chapter.verseCount, prefs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VerseUiState.Loading)

    // ── Auto-next countdown ───────────────────────────────────────────────────
    private val _autoNextCountdown = MutableStateFlow<Int?>(null)
    val autoNextCountdown: StateFlow<Int?> = _autoNextCountdown.asStateFlow()

    /** Emits (chapterNumber, verseNumber) when auto-next fires. Collected by the screen. */
    private val _navigateToVerse = MutableSharedFlow<Pair<Int, Int>>()
    val navigateToVerse: SharedFlow<Pair<Int, Int>> = _navigateToVerse.asSharedFlow()

    private var autoNextJob: Job? = null

    init {
        viewModelScope.launch { repository.updateReadingProgress(chapterNumber, verseNumber) }
        // Start auto-next if enabled
        viewModelScope.launch {
            preferencesManager.readingPreferences.collect { prefs ->
                if (prefs.autoNextEnabled) startAutoNext(prefs.autoNextIntervalSeconds)
                else stopAutoNext()
            }
        }
    }

    private fun startAutoNext(intervalSeconds: Int) {
        autoNextJob?.cancel()
        autoNextJob = viewModelScope.launch {
            var remaining = intervalSeconds
            while (remaining > 0) {
                _autoNextCountdown.value = remaining
                delay(1_000L)
                remaining--
            }
            _autoNextCountdown.value = null
            val state = uiState.value as? VerseUiState.Success ?: return@launch
            if (state.verse.verseNumber < state.totalVersesInChapter) {
                _navigateToVerse.emit(Pair(chapterNumber, verseNumber + 1))
            }
        }
    }

    private fun stopAutoNext() {
        autoNextJob?.cancel()
        autoNextJob = null
        _autoNextCountdown.value = null
    }

    fun resetAutoNext() {
        val prefs = (uiState.value as? VerseUiState.Success)?.preferences ?: return
        if (prefs.autoNextEnabled) startAutoNext(prefs.autoNextIntervalSeconds)
    }

    fun toggleBookmark() { viewModelScope.launch { repository.toggleBookmark(verseId) } }

    fun toggleFocusMode() {
        viewModelScope.launch {
            val cur = (uiState.value as? VerseUiState.Success)?.preferences?.focusModeEnabled ?: false
            preferencesManager.setFocusMode(!cur)
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoNextJob?.cancel()
    }
}
