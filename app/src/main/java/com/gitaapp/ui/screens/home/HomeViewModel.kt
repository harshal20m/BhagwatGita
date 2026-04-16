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
        viewModelScope.launch {
            combine(
                repository.observeVerseCount(),
                prefsManager.verseOfTheDayId,
                prefsManager.verseOfTheDayLastUpdate
            ) { count, savedId, lastUpdate ->
                Triple(count, savedId, lastUpdate)
            }.collect { (count, savedId, lastUpdate) ->
                if (count > 0) {
                    val today = getTodayMillis()
                    if (savedId != null && lastUpdate == today) {
                        // Load saved verse
                        if (_verseOfTheDay.value?.id != savedId) {
                            repository.observeVerse(savedId).collect { verse ->
                                _verseOfTheDay.value = verse
                            }
                        }
                    } else {
                        // Refresh verse for the new day
                        refreshVerseOfTheDay()
                    }
                }
            }
        }
    }

    fun refreshVerseOfTheDay() {
        viewModelScope.launch {
            val verse = repository.getRandomVerse()
            if (verse != null) {
                _verseOfTheDay.value = verse
                prefsManager.setVerseOfTheDay(verse.id, getTodayMillis())
            }
        }
    }

    private fun getTodayMillis(): Long {
        return java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}