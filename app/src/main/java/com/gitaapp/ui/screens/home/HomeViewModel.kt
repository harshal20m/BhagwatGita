package com.gitaapp.ui.screens.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaapp.core.di.PreferencesManager
import com.gitaapp.core.repository.GitaRepository
import com.gitaapp.data.model.AppLanguage
import com.gitaapp.data.model.Chapter
import com.gitaapp.data.model.ReadingProgress
import com.gitaapp.data.model.Verse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HomeUiState(
    val chaptersState: ChaptersState = ChaptersState.Loading,
    val lastReadProgress: ReadingProgress? = null,
    val overallProgress: Float = 0f,
    val bookmarkCount: Int = 0,
    val language: AppLanguage = AppLanguage.ENGLISH
)

sealed interface ChaptersState {
    data object Loading : ChaptersState
    data class Success(val chapters: List<Chapter>) : ChaptersState
    data class Error(val message: String) : ChaptersState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GitaRepository,
    private val prefsManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeAllChapters(),
        repository.observeLastReadChapter(),
        repository.observeOverallProgress(),
        repository.observeBookmarkCount(),
        prefsManager.readingPreferences.map { it.language }
    ) { chapters, lastRead, overall, bookmarks, lang ->
        HomeUiState(
            chaptersState    = if (chapters.isEmpty()) ChaptersState.Loading else ChaptersState.Success(chapters),
            lastReadProgress = lastRead,
            overallProgress  = overall,
            bookmarkCount    = bookmarks,
            language         = lang
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _verseOfTheDay = MutableStateFlow<Verse?>(null)
    val verseOfTheDay: StateFlow<Verse?> = _verseOfTheDay

    init {
        loadVerseOfTheDay()
        // Ensure we load the verse once data is available (e.g., after first-time seeding)
        viewModelScope.launch {
            repository.observeAllChapters().collect { chapters ->
                if (chapters.isNotEmpty() && _verseOfTheDay.value == null) {
                    loadVerseOfTheDay()
                }
            }
        }
    }

    fun refreshVerseOfTheDay() { loadVerseOfTheDay() }

    private fun loadVerseOfTheDay() {
        viewModelScope.launch { _verseOfTheDay.value = repository.getRandomVerse() }
    }
}