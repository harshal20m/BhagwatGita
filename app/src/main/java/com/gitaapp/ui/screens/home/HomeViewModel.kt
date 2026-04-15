package com.gitaapp.ui.screens.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.ReadingProgress
import com.gitaapp.data.model.Verse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

@Immutable
data class HomeUiState(
    val chaptersState: ChaptersState = ChaptersState.Loading,
    val verseOfTheDay: Verse? = null,
    val lastReadProgress: ReadingProgress? = null,
    val overallProgress: Float = 0f,
    val bookmarkCount: Int = 0
)

sealed interface ChaptersState {
    data object Loading : ChaptersState
    data class Success(val chapters: List<Chapter>) : ChaptersState
    data class Error(val message: String) : ChaptersState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GitaRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeAllChapters(),
        repository.observeLastReadChapter(),
        repository.observeOverallProgress(),
        repository.observeBookmarkCount()
    ) { chapters, lastRead, overallProgress, bookmarkCount ->
        HomeUiState(
            chaptersState = if (chapters.isEmpty()) ChaptersState.Loading
                            else ChaptersState.Success(chapters),
            lastReadProgress = lastRead,
            overallProgress = overallProgress,
            bookmarkCount = bookmarkCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _verseOfTheDay = MutableStateFlow<Verse?>(null)
    val verseOfTheDay: StateFlow<Verse?> = _verseOfTheDay

    init {
        loadVerseOfTheDay()
    }

    private fun loadVerseOfTheDay() {
        viewModelScope.launch {
            _verseOfTheDay.value = repository.getRandomVerse()
        }
    }

    fun refreshVerseOfTheDay() {
        loadVerseOfTheDay()
    }
}
