package com.gitaapp.ui.screens.chapter

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.Verse
import com.gitaapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ChapterUiState {
    data object Loading : ChapterUiState
    @Immutable
    data class Success(val chapter: Chapter, val verses: List<Verse>) : ChapterUiState
    data class Error(val message: String) : ChapterUiState
}

@HiltViewModel
class ChapterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GitaRepository
) : ViewModel() {

    private val chapterNumber: Int = checkNotNull(
        savedStateHandle[Screen.Chapter.ARG_CHAPTER_NUMBER]
    )

    val uiState: StateFlow<ChapterUiState> = combine(
        repository.observeChapter(chapterNumber),
        repository.observeVersesForChapter(chapterNumber)
    ) { chapter, verses ->
        when {
            chapter == null || verses.isEmpty() -> ChapterUiState.Loading
            else -> ChapterUiState.Success(chapter = chapter, verses = verses)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChapterUiState.Loading)

    // ── Read-only mode toggle — show just Sanskrit shlokas ────────────────
    private val _showOnlyVerses = MutableStateFlow(false)
    val showOnlyVerses: StateFlow<Boolean> = _showOnlyVerses.asStateFlow()

    fun toggleReadMode() {
        _showOnlyVerses.value = !_showOnlyVerses.value
    }
}
